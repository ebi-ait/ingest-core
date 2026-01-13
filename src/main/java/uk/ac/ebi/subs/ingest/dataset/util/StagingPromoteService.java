package uk.ac.ebi.subs.ingest.dataset.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetCommandInvocationRequest;
import software.amazon.awssdk.services.ssm.model.GetCommandInvocationResponse;
import software.amazon.awssdk.services.ssm.model.SendCommandRequest;
import software.amazon.awssdk.services.ssm.model.SendCommandResponse;
import uk.ac.ebi.subs.ingest.dataset.Dataset;
import uk.ac.ebi.subs.ingest.dataset.DatasetRepository;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class StagingPromoteService {

    private final SsmClient ssm;
    private final S3StagingProperties props;
    private final @NonNull DatasetRepository datasetRepository;

    private static final Pattern TASK_ID_PATTERN = Pattern.compile("TASK_ID=([a-f0-9-]+)");
    private static final Pattern SHA256_PATTERN = Pattern.compile("SHA256=([a-f0-9]{64})");

    public PromoteResponse promoteMetadata(String datasetId, String promotedBy) {
        Dataset d = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new IllegalArgumentException("Dataset not found: " + datasetId));

        String status = MetadataUploadResolver.currentStatus(d);
        if (!"UPLOADED_CONFIRMED".equals(status)) {
            throw new IllegalStateException("Latest metadata upload is not confirmed. status=" + status);
        }

        String key = MetadataUploadResolver.currentKey(d);
        if (key == null) {
            throw new IllegalStateException("No uploaded metadata found for dataset " + datasetId);
        }

        String bucket = props.getBucket(); // bucket from config (no resolver)

        String stagingDir = props.getStagingBaseDir() + "/" + datasetId;
        String localFile = stagingDir + "/metadata.xlsx";
        String destPath = "/" + datasetId + "/metadata.xlsx";

        String globus = props.getGlobusCli();
        String runAs = props.getGlobusRunAsUser();
        String srcEndpoint = props.getSrcEndpointId();
        String destEndpoint = props.getDestEndpointId();

        List<String> commands = List.of(
                "set -euo pipefail",
                "DATASET_ID=\"" + datasetId + "\"",
                "BUCKET=\"" + bucket + "\"",
                "KEY=\"" + key + "\"",
                "STAGING_DIR=\"" + stagingDir + "\"",
                "LOCAL_FILE=\"" + localFile + "\"",
                "DEST_PATH=\"" + destPath + "\"",
                "SRC_ENDPOINT=\"" + srcEndpoint + "\"",
                "DEST_ENDPOINT=\"" + destEndpoint + "\"",
                "GLOBUS_CLI=\"" + globus + "\"",
                "RUN_AS=\"" + runAs + "\"",

                "mkdir -p \"${STAGING_DIR}\"",
                "echo \"Downloading s3://${BUCKET}/${KEY} -> ${LOCAL_FILE}\"",
                "aws s3 cp \"s3://${BUCKET}/${KEY}\" \"${LOCAL_FILE}\"",

                // make sure ec2-user can clean it later even if command ran as root
                "chown -R ec2-user:ec2-user \"${STAGING_DIR}\" || true",
                "chmod -R u+rwX \"${STAGING_DIR}\" || true",

                // fingerprint
                "SHA256=$(sha256sum \"${LOCAL_FILE}\" | awk '{print $1}')",
                "echo \"SHA256=${SHA256}\"",

                "echo \"Submitting Globus transfer...\"",
                "OUT=$(sudo -u \"${RUN_AS}\" \"${GLOBUS_CLI}\" transfer " +
                        "\"${SRC_ENDPOINT}:${LOCAL_FILE}\" " +
                        "\"${DEST_ENDPOINT}:${DEST_PATH}\" " +
                        "--label \"morphic promote ${DATASET_ID} metadata\" " +
                        "--sync-level checksum)",

                "echo \"${OUT}\"",
                "TASK_ID=$(echo \"${OUT}\" | awk '/Task ID:/ {print $3}')",
                "echo \"TASK_ID=${TASK_ID}\"",

                // IMPORTANT: do NOT delete LOCAL_FILE here if EC2 is the source endpoint.
                "echo \"To cleanup later: sudo /usr/local/bin/morphic-staging-cleanup ${DATASET_ID}\""
        );

        log.warn("[Promote] runner={} src={} dest={} globusCli={} runAs={}",
                props.getRunnerInstanceId(), srcEndpoint, destEndpoint, globus, runAs);

        SendCommandResponse resp = ssm.sendCommand(
                SendCommandRequest.builder()
                        .documentName(props.getSsmDocumentName())
                        .instanceIds(props.getRunnerInstanceId())
                        .parameters(Map.of("commands", commands))
                        .comment("morphic metadata promote " + datasetId)
                        .build()
        );

        String commandId = resp.command().commandId();

        // persist minimal provenance immediately
        recordPromoteSubmitted(d, commandId, promotedBy, bucket, key, destEndpoint, destPath);

        return new PromoteResponse(datasetId, commandId);
    }

    public CommandStatusResponse status(String datasetId, String commandId) {
        GetCommandInvocationResponse inv =
                ssm.getCommandInvocation(GetCommandInvocationRequest.builder()
                        .commandId(commandId)
                        .instanceId(props.getRunnerInstanceId())
                        .build());

        String stdout = inv.standardOutputContent();
        String stderr = inv.standardErrorContent();

        String taskId = null;
        String sha256 = null;

        if (stdout != null) {
            Matcher m = TASK_ID_PATTERN.matcher(stdout);
            if (m.find()) taskId = m.group(1);

            Matcher s = SHA256_PATTERN.matcher(stdout);
            if (s.find()) sha256 = s.group(1);
        }

        // truncate stdout for storage (and only store if not Success)
        String stdoutShort = stdout == null ? null : stdout.substring(0, Math.min(stdout.length(), 2000));

        recordPromoteResult(
                datasetId,
                commandId,
                inv.statusAsString(),
                taskId,
                sha256,
                stdoutShort
        );

        return new CommandStatusResponse(inv.statusAsString(), taskId, stdout, stderr);
    }

    @SuppressWarnings("unchecked")
    private void recordPromoteSubmitted(
            Dataset d,
            String ssmCommandId,
            String promotedBy,
            String bucket,
            String key,
            String destEndpoint,
            String destPath) {

        Map<String, Object> content = uk.ac.ebi.subs.ingest.dataset.util.ContentMaps.asMap(d.getContent());
        Object existing = content.get("metadataUpload");
        Map<String, Object> mu;
        if (existing instanceof Map) {
            mu = (Map<String, Object>) existing;
        } else {
            mu = new LinkedHashMap<>();
            content.put("metadataUpload", mu);
        }

        Map<String, Object> lp = new LinkedHashMap<>();
        lp.put("submittedAt", Instant.now().toString());
        lp.put("ssmCommandId", ssmCommandId);
        lp.put("ssmStatus", "SUBMITTED");
        if (promotedBy != null) lp.put("promotedBy", promotedBy);

        Map<String, Object> source = new LinkedHashMap<>();
        source.put("attemptId", MetadataUploadResolver.currentAttemptId(d));
        source.put("key", key);
        source.put("etag", MetadataUploadResolver.currentETag(d));
        lp.put("source", source);

        Map<String, Object> dest = new LinkedHashMap<>();
        dest.put("endpointId", destEndpoint);
        dest.put("path", destPath);
        lp.put("dest", dest);

        mu.put("lastPromote", lp);

        d.setContent(content);
        datasetRepository.save(d);
    }

    @SuppressWarnings("unchecked")
    private void recordPromoteResult(
            String datasetId,
            String ssmCommandId,
            String ssmStatus,
            String globusTaskId,
            String sha256,
            String stdoutSnippet) {

        Dataset d = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new IllegalArgumentException("Dataset not found: " + datasetId));

        Map<String, Object> content = uk.ac.ebi.subs.ingest.dataset.util.ContentMaps.asMap(d.getContent());
        Object muObj = content.get("metadataUpload");
        if (!(muObj instanceof Map)) return;

        Map<String, Object> mu = (Map<String, Object>) muObj;
        Object lpObj = mu.get("lastPromote");
        if (!(lpObj instanceof Map)) return;

        Map<String, Object> lp = (Map<String, Object>) lpObj;

        // avoid overwriting a newer promotion
        Object existing = lp.get("ssmCommandId");
        if (existing != null && !existing.toString().equals(ssmCommandId)) return;

        lp.put("updatedAt", Instant.now().toString());
        lp.put("ssmStatus", ssmStatus);

        if (globusTaskId != null) lp.put("globusTaskId", globusTaskId);
        if (sha256 != null) {
            Object sourceObj = lp.get("source");
            if (sourceObj instanceof Map) {
                ((Map<String, Object>) sourceObj).put("sha256", sha256);
            }
        }

        // store stdout only on failure/non-success
        if (stdoutSnippet != null && !"Success".equalsIgnoreCase(ssmStatus)) {
            lp.put("stdout", stdoutSnippet);
        } else {
            lp.remove("stdout");
        }

        d.setContent(content);
        datasetRepository.save(d);
    }
}

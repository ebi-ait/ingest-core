package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.core.MetadataDocumentMessageBuilder;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.messaging.model.ExportMessage;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;

import java.util.UUID;

public class ExportData {

    private final int index;
    private final int totalCount;

    private final Process process;

    private final SubmissionEnvelope submissionEnvelope;

    public ExportData(int index, int totalCount, Process process, SubmissionEnvelope
            submissionEnvelope) {
        this.index = index;
        this.totalCount = totalCount;
        this.process = process;
        this.submissionEnvelope = submissionEnvelope;
    }

    public Integer getIndex() {
        return index;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public Process getProcess() {
        return process;
    }

    public SubmissionEnvelope getSubmissionEnvelope() {
        return submissionEnvelope;
    }

    public ExportMessage toAssaySubmittedMessage(LinkGenerator linkGenerator) {
        Uuid submissionUuid = submissionEnvelope.getUuid();
        MetadataDocumentMessageBuilder builder = MetadataDocumentMessageBuilder.using(linkGenerator)
                .messageFor(process)
                .withEnvelopeId(submissionEnvelope.getId())
                .withAssayIndex(index)
                .withTotalAssays(totalCount);

        if(submissionUuid != null && submissionUuid.getUuid() != null){
            builder.withEnvelopeUuid(submissionUuid.getUuid().toString());
        }

        return builder.buildAssaySubmittedMessage();
    }

}

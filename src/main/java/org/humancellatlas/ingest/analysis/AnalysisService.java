package org.humancellatlas.ingest.analysis;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@Service
@RequiredArgsConstructor
@Getter
public class AnalysisService {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull AnalysisRepository analysisRepository;
    private final @NonNull BundleManifestRepository bundleManifestRepository;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Analysis addAnalysisToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Analysis analysis) {
        analysis.addToSubmissionEnvelope(submissionEnvelope);
        return getAnalysisRepository().save(analysis);
    }

    public Analysis resolveBundleReferencesForAnalysis(Analysis analysis, BundleReference bundleReference) {
        for (String bundleUuid : bundleReference.getBundleUuids()) {
            BundleManifest bundleManifest = getBundleManifestRepository().findByBundleUuid(bundleUuid);
            if (bundleManifest != null) {
                getLog().info(String.format("Adding bundle manifest link to analysis '%s'", analysis.getId()));
                analysis.getInputBundleManifests().add(bundleManifest);
            }
            else {
                getLog().warn(String.format(
                        "No Bundle Manifest present with bundle UUID '%s' - in future this will cause a critical error",
                        bundleUuid));
            }
        }
        return analysisRepository.save(analysis);
    }
}

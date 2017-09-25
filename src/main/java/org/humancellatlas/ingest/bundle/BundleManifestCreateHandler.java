package org.humancellatlas.ingest.bundle;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 25/09/17
 */
@Component
@RepositoryEventHandler
@RequiredArgsConstructor
public class BundleManifestCreateHandler {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @HandleAfterCreate
    public void handleBundleManifestCreation(BundleManifest bundleManifest) {
        if (bundleManifest.getEnvelopeUuid() != null) {
            SubmissionEnvelope se = submissionEnvelopeRepository.findByUuid(new Uuid(bundleManifest.getEnvelopeUuid()));

            if (se != null) {
                se.addCreatedBundleManifest(bundleManifest);

                submissionEnvelopeRepository.save(se);
            }
            else {
                getLog().warn("Bundle Manifest '%s' references submission envelope with UUID = '%s'; " +
                                      "this submission envelope could not be found so the bundle manifest " +
                                      "reference was not set",
                              bundleManifest.getId(),
                              bundleManifest.getEnvelopeUuid());
            }
        }
    }
}

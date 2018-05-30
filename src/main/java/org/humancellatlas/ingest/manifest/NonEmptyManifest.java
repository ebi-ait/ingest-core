package org.humancellatlas.ingest.manifest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;

/**
 * Created by rolando on 30/05/2018.
 */
@AllArgsConstructor
@Getter
public class NonEmptyManifest extends AbstractEntity implements SubmissionManifest {
    private final int expectedBiomaterials;
    private final int expectedProcesses;
    private final int expectedFiles;
    private final int expectedProtocols;
    private final int expectedProjects;

    private final @DBRef SubmissionEnvelope submissionEnvelope;
}

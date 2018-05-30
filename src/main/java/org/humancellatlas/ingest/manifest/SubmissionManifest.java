package org.humancellatlas.ingest.manifest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;

/**
 * Created by rolando on 30/05/2018.
 */
@AllArgsConstructor
@Getter
public class SubmissionManifest extends AbstractEntity {
    private final Integer expectedBiomaterials;
    private final Integer expectedProcesses;
    private final Integer expectedFiles;
    private final Integer expectedProtocols;
    private final Integer expectedProjects;

    @Setter private @DBRef SubmissionEnvelope submissionEnvelope;
}

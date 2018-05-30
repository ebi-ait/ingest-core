package org.humancellatlas.ingest.submission.manifest;

import org.humancellatlas.ingest.core.AbstractEntity;

/**
 * Created by rolando on 30/05/2018.
 */
public class NonEmptyManifest extends AbstractEntity implements SubmissionManifest {
    private int expectedBiomaterials;
    private int expectedProcesses;
    private int expectedFiles;
    private int expectedProtocols;
    private int expectedProjects;
}

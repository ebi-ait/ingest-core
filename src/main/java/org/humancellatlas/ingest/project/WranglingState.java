package org.humancellatlas.ingest.project;

public enum WranglingState {
    NEW,
    ELIGIBLE,
    NOT_ELIGIBLE,
    IN_PROGRESS,
    STALLED,
    SUBMITTED,
    PUBLISHED_IN_DCP,
    DELETED
}

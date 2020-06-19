package org.humancellatlas.ingest.archiving.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ArchiveEntityTypeSerializer.class)
public enum ArchiveEntityType {
    SAMPLE("sample"),
    PROJECT("project"),
    STUDY("study"),
    SEQUENCING_EXPERIMENT("sequencingExperiment"),
    SEQUENCING_RUN("sequencingRun");

    protected String type;

    ArchiveEntityType(String type) {
        this.type = type;
    }
}


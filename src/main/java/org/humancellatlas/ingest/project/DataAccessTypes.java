package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.humancellatlas.ingest.archiving.entity.ArchiveEntityTypeSerializer;

@JsonSerialize(using = ArchiveEntityTypeSerializer.class)
public enum DataAccessTypes {
    OPEN("All fully open"),
    MANAGED("All managed access"),
    MIXTURE("A mixture of open and managed"),
    COMPLICATED("It's complicated"),
    SEQUENCING_RUN("sequencingRun");

    protected String type;

    DataAccessTypes(String type) {
        this.type = type;
    }
}

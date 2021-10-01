package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import org.humancellatlas.ingest.archiving.entity.ArchiveEntityTypeSerializer;

@JsonSerialize(using = ArchiveEntityTypeSerializer.class)
public enum DataAccessTypes {
    OPEN("All fully open"),
    MANAGED("All managed access"),
    MIXTURE("A mixture of open and managed"),
    COMPLICATED("It's complicated"),

    @Getter
    final String label;

    DataAccessTypes(String label) {
        this.label = label;
    }
}

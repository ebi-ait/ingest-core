package org.humancellatlas.ingest.project;

import lombok.Getter;

public enum DataAccessTypes {
    OPEN("All fully open"),
    MANAGED("All managed access"),
    MIXTURE("A mixture of open and managed"),
    COMPLICATED("It's complicated");

    @Getter
    final String label;

    DataAccessTypes(String label) {
        this.label = label;
    }
}

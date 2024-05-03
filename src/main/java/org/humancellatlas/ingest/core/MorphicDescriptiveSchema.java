package org.humancellatlas.ingest.core;

public interface MorphicDescriptiveSchema {
    String getDescribedBy();
    void setDescribedBy(String describedBy);

    String getSchemaVersion();
    void setSchemaVersion(String schemaVersion);

    String getSchemaType();
    void setSchemaType(String schemaType);
}
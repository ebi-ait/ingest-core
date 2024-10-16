package uk.ac.ebi.subs.ingest.core;

public interface DescriptiveSchema {
  String getDescribedBy();

  void setDescribedBy(String describedBy);

  String getSchemaVersion();

  void setSchemaVersion(String schemaVersion);

  String getSchemaType();

  void setSchemaType(String schemaType);
}

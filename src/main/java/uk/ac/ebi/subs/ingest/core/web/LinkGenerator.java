package uk.ac.ebi.subs.ingest.core.web;

public interface LinkGenerator {

  String createCallback(Class<?> documentType, String documentId);
}

package org.humancellatlas.ingest.core.web;

public interface LinkGenerator {

  String createCallback(Class<?> documentType, String documentId);
}

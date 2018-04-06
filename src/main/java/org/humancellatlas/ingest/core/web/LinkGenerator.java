package org.humancellatlas.ingest.core.web;

import org.humancellatlas.ingest.core.MetadataDocument;

public interface LinkGenerator {

    String createCallback(MetadataDocument document);

}

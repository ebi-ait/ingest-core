package org.humancellatlas.ingest.core;

/**
 * Created by rolando on 26/09/2017.
 */

public interface AbstractEntityMessage {
    String getDocumentUuid();
    String getDocumentType();
    String getDocumentId();
    String getCallbackLink();
}

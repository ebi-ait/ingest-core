package org.humancellatlas.ingest.messaging;

public class ExportMessage {

    private final int index;

    public ExportMessage(int index) {
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }

}

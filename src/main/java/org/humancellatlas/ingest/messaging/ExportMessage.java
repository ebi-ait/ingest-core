package org.humancellatlas.ingest.messaging;

public class ExportMessage {

    private final int index;
    private final int totalCount;

    public ExportMessage(int index, int totalCount) {
        this.index = index;
        this.totalCount = totalCount;
    }

    public Integer getIndex() {
        return index;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

}

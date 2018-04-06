package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.process.Process;

public class ExportMessage {

    private final int index;
    private final int totalCount;
    private final Process process;

    public ExportMessage(int index, int totalCount, Process process) {
        this.index = index;
        this.totalCount = totalCount;
        this.process = process;
    }

    public Integer getIndex() {
        return index;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public Process getProcess() {
        return process;
    }

}

package org.humancellatlas.ingest.messaging;

import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;

public class ExportMessage {

    private final int index;
    private final int totalCount;

    private final Process process;

    private final SubmissionEnvelope submissionEnvelope;

    public ExportMessage(int index, int totalCount, Process process, SubmissionEnvelope
            submissionEnvelope) {
        this.index = index;
        this.totalCount = totalCount;
        this.process = process;
        this.submissionEnvelope = submissionEnvelope;
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

    public SubmissionEnvelope getSubmissionEnvelope() {
        return submissionEnvelope;
    }

}

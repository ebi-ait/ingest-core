package org.humancellatlas.ingest.file;

import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FileTest {

    @Test
    public void testAddToAnalysis() {
        //given:
        Process analysis = new Process();
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        analysis.addToSubmissionEnvelope(submissionEnvelope);

        //when:
        File file = new File();
        file.addToAnalysis(analysis);

        //then:
        assertThat(file.getDerivedByProcesses()).contains(analysis);
    }

}

package org.humancellatlas.ingest.file;

import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class FileTest {

    @Test
    public void testAddAsDerivedByProcess() {
        //given:
        Process process = createTestProcess();
        File file = new File();

        //when:
        file.addAsDerivedByProcess(process);

        //then:
        assertThat(file.getDerivedByProcesses()).containsExactly(process);
    }

    @Test
    public void testAddDerivedByProcessdNoDuplication() {
        //given:
        Process process = createTestProcess();
        File file = new File();

        //when:
        file.addAsDerivedByProcess(process);
        //and: add twice
        file.addAsDerivedByProcess(process);

        //then:
        assertThat(file.getDerivedByProcesses()).hasSize(1);
    }

    @Test
    public void testAddToAnalysis() {
        //given:
        Process analysis = createTestProcess();
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        analysis.addToSubmissionEnvelope(submissionEnvelope);

        //when:
        File file = new File();
        file.addToAnalysis(analysis);

        //then:
        assertThat(file.getDerivedByProcesses()).contains(analysis);
        assertThat(file.getSubmissionEnvelopes()).contains(submissionEnvelope);
    }

    @Test
    public void testAddToAnalysisWhenFileAlreadyLinkedToSubmissionEnvelope() {
        //given:
        Process analysis = createTestProcess();
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        analysis.addToSubmissionEnvelope(submissionEnvelope);

        //and:
        File file = new File();
        file.addToSubmissionEnvelope(submissionEnvelope);

        //when:
        file.addToAnalysis(analysis);

        //then:
        assertThat(file.getDerivedByProcesses()).contains(analysis);
        assertThat(file.getSubmissionEnvelopes()).contains(submissionEnvelope);
    }

    private Process createTestProcess() {
        Process process = spy(new Process());
        doReturn("fe89a0").when(process).getId();
        return process;
    }

}

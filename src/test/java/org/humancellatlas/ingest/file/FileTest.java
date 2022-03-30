package org.humancellatlas.ingest.file;

import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class FileTest {
    Process process;
    File file;

    @BeforeEach
    void setUp() {
        //given:
        process = spy(new Process(null));
        file = spy(new File(null, "fileName"));
    }

    @Test
    public void testAddAsDerivedByProcess() {
        //when:
        file.addAsDerivedByProcess(process);

        //then:
        assertThat(file.getDerivedByProcesses()).containsExactly(process);
    }

    @Test
    public void testAddDerivedByProcessdNoDuplication() {
        // given:
        doReturn("fe89a0").when(process).getId();

        // when:
        file.addAsDerivedByProcess(process);
        file.addAsDerivedByProcess(process);

        //then:
        assertThat(file.getDerivedByProcesses()).hasSize(1);
    }

    @Test
    public void testAddToAnalysis() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        process.setSubmissionEnvelope(submissionEnvelope);

        //when:
        file.addToAnalysis(process);

        //then:
        assertThat(file.getDerivedByProcesses()).contains(process);
        assertThat(file.getSubmissionEnvelope()).isEqualTo(submissionEnvelope);
    }

    @Test
    public void testAddToAnalysisWhenFileAlreadyLinkedToSubmissionEnvelope() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        process.setSubmissionEnvelope(submissionEnvelope);
        file.setSubmissionEnvelope(submissionEnvelope);

        //when:
        file.addToAnalysis(process);

        //then:
        assertThat(file.getDerivedByProcesses()).contains(process);
        assertThat(file.getSubmissionEnvelope()).isEqualTo(submissionEnvelope);
    }
}

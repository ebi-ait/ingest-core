package org.humancellatlas.ingest.file;

import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class FileTest {
    Process process;
    File file;

    @BeforeEach
    void setUp() {
        //given:
        file = new File(null, "fileName");

        process = spy(new Process(null));
        doReturn("fe89a0").when(process).getId();
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

    @ParameterizedTest
    @MethodSource("testFiles")
    public void newFileHasDataFileUuidNotNull(File file) {
        assertThat(file)
                .extracting("dataFileUuid")
                .doesNotContainNull();
    }

    private static Stream<File> testFiles() {
        return Stream.of(
                new File(),
                new File(null, "test-File")
        );
    }
}

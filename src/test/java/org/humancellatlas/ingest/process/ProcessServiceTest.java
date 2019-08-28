package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.state.MetadataDocumentEventHandler;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class ProcessServiceTest {

    @Autowired
    private ProcessService service;

    @MockBean
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;
    @MockBean
    private ProcessRepository processRepository;
    @MockBean
    private FileRepository fileRepository;
    @MockBean
    private BiomaterialRepository biomaterialRepository;
    @MockBean
    private BundleManifestRepository bundleManifestRepository;
    @MockBean
    private MetadataDocumentEventHandler metadataDocumentEventHandler;
    @MockBean
    private MetadataCrudService metadataCrudService;
    @MockBean
    private MetadataUpdateService metadataUpdateService;

    @Test
    public void testAddFileToAnalysisProcess() {
        //given:
        Process analysis = new Process();
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        analysis.addToSubmissionEnvelope(submissionEnvelope);

        //and:
        File file = new File();
        file.setFileName("ERR1630013.fastq.gz");
        file = spy(file);

        //and:
        doReturn(Collections.emptyList()).when(fileRepository)
                .findBySubmissionEnvelopesInAndFileName(any(SubmissionEnvelope.class), anyString());

        //when:
        Process result = service.addOutputFileToAnalysisProcess(analysis, file);

        //then:
        assertThat(result).isEqualTo(analysis);
        verify(file).addToAnalysis(analysis);
        verify(fileRepository).save(file);
    }

    @Test
    public void testAddFileToAnalysisProcessWhenFileAlreadyExists() {
        //given:
        Process analysis = new Process();
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        analysis.addToSubmissionEnvelope(submissionEnvelope);

        //and:
        File file = new File();
        String fileName = "ERR1630013.fastq.gz";
        file.setFileName(fileName);

        //and:
        File persistentFile = spy(new File());
        List<File> persistentFiles = Arrays.asList(persistentFile);
        doReturn(persistentFiles).when(fileRepository)
                .findBySubmissionEnvelopesInAndFileName(submissionEnvelope, fileName);

        //when:
        Process result = service.addOutputFileToAnalysisProcess(analysis, file);

        //then:
        assertThat(result).isEqualTo(analysis);

        //and:
        verify(persistentFile).addToAnalysis(analysis);
        verify(fileRepository).save(persistentFile);
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        ProcessService processService() {
            return new ProcessService();
        }

    }

}

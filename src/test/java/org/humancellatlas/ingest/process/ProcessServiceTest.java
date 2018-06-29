package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.ResourceLinker;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
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
    private ResourceLinker resourceLinker;

    @Test
    public void testAddFileToAnalysisProcess() {
        //given:
        Process analysis = new Process();
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        analysis.addToSubmissionEnvelope(submissionEnvelope);

        //and:
        File file = spy(new File());

        //when:
        Process result = service.addFileToAnalysisProcess(analysis, file);

        //then:
        assertThat(result).isEqualTo(analysis);
        verify(file).addToSubmissionEnvelope(submissionEnvelope);
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
        Uuid fileUuid = new Uuid();
        file.setUuid(fileUuid);

        //and:
        File persistentFile = spy(new File());
        doReturn(persistentFile).when(fileRepository).findByUuid(fileUuid);

        //when:
        Process result = service.addFileToAnalysisProcess(analysis, file);

        //then:
        assertThat(result).isEqualTo(analysis);

        //and:
        verify(persistentFile).addToSubmissionEnvelope(submissionEnvelope);
        verify(persistentFile).addAsDerivedByProcess(analysis);
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

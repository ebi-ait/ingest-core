package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
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
        Process analysis = spy(new Process());
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        analysis.addToSubmissionEnvelope(submissionEnvelope);

        File file = spy(new File());

        //when:
        service.addFileToAnalysisProcess(analysis, file);

        //then:
        verify(file).addToSubmissionEnvelope(submissionEnvelope);
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        ProcessService processService() {
            return new ProcessService();
        }

    }

}

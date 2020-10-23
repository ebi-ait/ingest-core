package org.humancellatlas.ingest.protocol;

import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes=ProtocolService.class)
public class ProtocolServiceTest {

    @Autowired
    private ProtocolService protocolService;

    @MockBean
    private MetadataCrudService metadataCrudService;

    @MockBean
    private MetadataUpdateService metadataUpdateService;

    @MockBean
    private ProtocolRepository protocolRepository;

    @MockBean
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @Nested
    class Submission {

        @Test
        void determineLinking() {
            //given:
            SubmissionEnvelope submission = new SubmissionEnvelope("89bcba7");

            //when:
            Page<Protocol> results = protocolService.retrieve(submission, mock(Pageable.class));

            //then:
            assertThat(results).isNotNull();
        }

    }

}

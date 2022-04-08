package org.humancellatlas.ingest.protocol;

import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
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
    private ProcessRepository processRepository;

    @MockBean
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @Nested
    class Submission {

        @Test
        void determineLinking() {
            //given:
            SubmissionEnvelope submission = new SubmissionEnvelope();

            //and:
            Protocol linked = new Protocol("linked");
            Protocol notLinked = new Protocol("not linked");
            Pageable pageable = mock(Pageable.class);
            doReturn(new PageImpl(asList(linked, notLinked))).when(protocolRepository)
                    .findBySubmissionEnvelope(submission, pageable);

            //and:
            doReturn(Optional.of(new Process(null))).when(processRepository).findFirstByProtocolsContains(linked);
            doReturn(Optional.empty()).when(processRepository).findFirstByProtocolsContains(notLinked);

            //when:
            Page<Protocol> results = protocolService.retrieve(submission, pageable);

            //then:
            assertThat(results).isNotNull();
            assertThat(results.getTotalElements()).isEqualTo(2);

            //and:
            assertThat(linked.isLinked()).isTrue();
            assertThat(notLinked.isLinked()).isFalse();
        }

    }

}

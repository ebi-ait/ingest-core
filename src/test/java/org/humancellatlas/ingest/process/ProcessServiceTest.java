package org.humancellatlas.ingest.process;

import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessServiceTest {

    @Configuration
    static class TestConfiguration {

        @Bean
        ProcessService processService() {
            return new ProcessService();
        }

    }

    @Autowired
    private ProcessService processService;

    @MockBean
    private ProcessRepository processRepository;

    @Test
    @WithMockUser
    public void testRetrieveAssays() throws Exception {
        //given:
        Process assayingProcess = mock(Process.class);
        doReturn(true).when(assayingProcess).isAssaying();

        //and:
        Process nonAssayingProcess = mock(Process.class);
        doReturn(false).when(nonAssayingProcess).isAssaying();

        //and:
        Page<Process> processes = new PageImpl<>(asList(assayingProcess, nonAssayingProcess));
        doReturn(processes).when(processRepository).findBySubmissionEnvelopesContaining(
                any(SubmissionEnvelope.class), any(Pageable.class));

        //and:
        SubmissionEnvelope envelope = new SubmissionEnvelope();
        Pageable pageable = new PageRequest(1, 10);

        //when:
        Page<Process> assays = processService.retrieveAssaysFrom(envelope, pageable);

        //then:
        assertThat(assays.getContent()).containsExactly(assayingProcess);
    }

}

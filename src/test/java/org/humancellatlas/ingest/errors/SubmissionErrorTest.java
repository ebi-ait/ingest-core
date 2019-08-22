package org.humancellatlas.ingest.errors;

import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import org.zalando.problem.*;

import java.net.URI;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SubmissionErrorService.class})
public class SubmissionErrorTest {
    @MockBean
    private Pageable pageable;
    @MockBean
    private SubmissionErrorRepository submissionErrorRepository;
    @Autowired
    private SubmissionErrorService submissionErrorService;

    @Test
    public void serviceCallsRepository() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        //and:
        when(submissionErrorRepository.findBySubmissionEnvelope(any(SubmissionEnvelope.class), any(Pageable.class)))
                .thenReturn(new PageImpl<SubmissionError>(Collections.emptyList()));

        //then:
        assertThat(submissionErrorService.getErrorsFromEnvelope(submissionEnvelope,pageable)).isEmpty();

    }

    @Test
    public void errorIsGivenEnvelope() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        ArgumentCaptor<SubmissionError> insertedError = ArgumentCaptor.forClass(SubmissionError.class);

        //when:
        SubmissionError submissionError = submissionErrorService.addErrorToEnvelope(submissionEnvelope, randomProblem());

        //then:
        verify(submissionErrorRepository).insert(insertedError.capture());
        assertThat(insertedError.getValue().getSubmissionEnvelope()).isEqualTo(submissionEnvelope);
        assertThat(insertedError.getValue()).isEqualTo(submissionError);
    }

    @Test
    public void problemHasInstanceRemoved() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        ArgumentCaptor<SubmissionError> insertedError = ArgumentCaptor.forClass(SubmissionError.class);

        //when:
        submissionErrorService.addErrorToEnvelope(submissionEnvelope, randomProblem());

        //then:
        verify(submissionErrorRepository).insert(insertedError.capture());
        assertThat(insertedError.getValue().getInstance()).isNull();
    }

    @Test
    public void problemHasStatusRemoved() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        ArgumentCaptor<SubmissionError> insertedError = ArgumentCaptor.forClass(SubmissionError.class);

        //when:
        submissionErrorService.addErrorToEnvelope(submissionEnvelope, randomProblem());

        //then:
        verify(submissionErrorRepository).insert(insertedError.capture());
        assertThat(insertedError.getValue().getStatus()).isNull();
    }

    public static Problem randomProblem() {
        Random random = new Random();
        StatusType status;
        URI baseType = URI.create("http://test.ingest.data.humancellatlas.org/");
        String type;
        if (random.nextBoolean()) {
            status = Status.valueOf(400);
            type = "Error";
        } else {
            status = Status.valueOf(300);
            type = "Warning";
        }

        return Problem.builder()
                .withStatus(status)
                .withType(baseType.resolve(type))
                .withTitle("Random " + type)
                .withDetail(UUID.randomUUID().toString() + UUID.randomUUID().toString())
                .withInstance(baseType.resolve(type + "/" + UUID.randomUUID()))
                .build();
    }
}

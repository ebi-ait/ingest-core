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

import java.util.*;

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
    SubmissionErrorService submissionErrorService;

    @Test
    public void serviceCallsRepository() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        //and:
        when(submissionErrorRepository.findBySubmissionEnvelope(any(SubmissionEnvelope.class), any(Pageable.class)))
                .thenReturn(new PageImpl(Collections.emptyList()));

        //then:
        assertThat(submissionErrorService.getErrorsFromEnvelope(submissionEnvelope,pageable)).isEmpty();

    }

    @Test
    public void errorIsGivenEnvelope() {
        //given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        SubmissionError error = randErrorMessage();
        ArgumentCaptor<SubmissionError> insertedError = ArgumentCaptor.forClass(SubmissionError.class);

        //when:
        submissionErrorService.addErrorToEnvelope(submissionEnvelope, error);
        verify(submissionErrorRepository).insert(insertedError.capture());

        //then:
        assertThat(error.getSubmissionEnvelope()).isEqualTo(submissionEnvelope);
        assertThat(insertedError.getValue()).isEqualTo(error);
    }

    public static SubmissionError randErrorMessage() {
        Random random = new Random();
        SubmissionError newError = new SubmissionError();
        if (random.nextBoolean()) {
            newError.setErrorType(ErrorType.ERROR);
        } else {
            newError.setErrorType(ErrorType.WARNING);
        }
        newError.setErrorCode(String.format("%1$04d", random.nextInt(10000)));
        newError.setMessage(Long.toString(random.nextLong()));
        return newError;
    }
}

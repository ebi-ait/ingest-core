package org.humancellatlas.ingest.errors;

import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SubmissionErrorService.class})
public class SubmissionErrorTest {
    @MockBean
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @Test
    public void newEnvelopeHasNoErrors() {
        //given:
        SubmissionErrorService service = new SubmissionErrorService(submissionEnvelopeRepository);
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        //then:
        assertThat(service.getErrorFromEnvelope(submissionEnvelope)).isEmpty();
    }

    @Test
    public void newErrorIsSaved() {
        //given:
        SubmissionErrorService service = new SubmissionErrorService(submissionEnvelopeRepository);
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        SubmissionError error = randErrorMessage();

        //when:
        service.addErrorToEnvelope(error, submissionEnvelope);

        //then:
        assertThat(service.getErrorFromEnvelope(submissionEnvelope)).containsOnlyOnce(error);
    }

    @Test
    public void newErrorsAreSavedInOrder() {
        //given:
        SubmissionErrorService service = new SubmissionErrorService(submissionEnvelopeRepository);
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        SubmissionError error1 = randErrorMessage();
        SubmissionError error2 = randErrorMessage();

        //when:
        service.addErrorToEnvelope(error1, submissionEnvelope);
        service.addErrorToEnvelope(error2, submissionEnvelope);

        //then:
        assertThat(service.getErrorFromEnvelope(submissionEnvelope)).containsOnlyOnce(error1);
        assertThat(service.getErrorFromEnvelope(submissionEnvelope)).containsOnlyOnce(error2);
        assertThat(service.getErrorFromEnvelope(submissionEnvelope)).containsSequence(error1, error2);
    }

    public SubmissionError randErrorMessage() {
        SubmissionError err = new SubmissionError();
        Random random = new Random();
        if (random.nextBoolean()) {
            err.setErrorType(ErrorType.ERROR);
        } else {
            err.setErrorType(ErrorType.WARNING);
        }
        err.setErrorCode(String.format("%1$04d", random.nextInt(10000)));
        err.setMessage(Long.toString(random.nextLong()));
        return err;
    }
}

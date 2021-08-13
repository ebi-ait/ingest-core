package org.humancellatlas.ingest.errors;

import org.humancellatlas.ingest.errors.web.SubmissionErrorController;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SubmissionErrorController.class})
public class SubmissionErrorControllerTest {
    @Autowired
    private SubmissionErrorController controller;

    @MockBean
    SubmissionErrorService submissionErrorService;

    @MockBean
    private PagedResourcesAssembler pagedResourcesAssembler;

    @Test
    public void testDeleteSubmissionErrors() {
        // given:
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();

        // when:
        ResponseEntity<SubmissionError> response = controller.deleteSubmissionEnvelopeErrors(submissionEnvelope);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(submissionErrorService).deleteSubmissionEnvelopeErrors(submissionEnvelope);
    }
}

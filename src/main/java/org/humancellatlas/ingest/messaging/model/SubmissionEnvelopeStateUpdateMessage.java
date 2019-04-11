package org.humancellatlas.ingest.messaging.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.state.SubmissionState;

/**
 * Created by rolando on 10/03/2018.
 */
public class SubmissionEnvelopeStateUpdateMessage extends SubmissionEnvelopeMessage {
    @Getter @Setter private SubmissionState requestedState;

    public SubmissionEnvelopeStateUpdateMessage(MessageProtocol messageProtocol, String documentType, String documentId, String documentUuid, String callbackLink) {
        super(messageProtocol, documentType, documentId, documentUuid, callbackLink);
    }

    @JsonIgnore
    public static SubmissionEnvelopeStateUpdateMessage fromSubmissionEnvelopeMessage(SubmissionEnvelopeMessage message) {
        return new SubmissionEnvelopeStateUpdateMessage(
                message.getMessageProtocol(),
                message.getDocumentType(),
                message.getDocumentId(),
                message.getDocumentUuid(),
                message.getCallbackLink());
    }
}

package org.humancellatlas.ingest.messaging;

import lombok.NoArgsConstructor;
import org.humancellatlas.ingest.config.ConfigurationService;
import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.exporter.ExporterData;
import org.humancellatlas.ingest.messaging.model.BundleUpdateMessage;
import org.humancellatlas.ingest.messaging.model.MetadataDocumentMessage;
import org.humancellatlas.ingest.messaging.model.SubmissionEnvelopeMessage;
import org.humancellatlas.ingest.messaging.model.SubmissionEnvelopeStateUpdateMessage;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeMessageBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.humancellatlas.ingest.messaging.Constants.Exchanges.ASSAY_EXCHANGE;
import static org.humancellatlas.ingest.messaging.Constants.Routing.*;

/**
 * Created by rolando on 09/03/2018.
 */
@Component
@NoArgsConstructor
public class MessageRouter {

    @Autowired
    private MessageSender messageSender;
    @Autowired
    private ResourceMappings resourceMappings;
    @Autowired
    private RepositoryRestConfiguration config;
    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private LinkGenerator linkGenerator;

    /* messages to validator */
    public boolean routeValidationMessageFor(MetadataDocument document) {
        if (document.getValidationState().equals(ValidationState.DRAFT)) {
            this.messageSender.queueValidationMessage(Constants.Exchanges.VALIDATION,
                    Constants.Queues.VALIDATION_REQUIRED,
                    messageFor(document),
                    document.getUpdateDate().toEpochMilli());
            return true;
        } else {
            return false;
        }
    }

    /* messages to accessioner */

    public boolean routeAccessionMessageFor(MetadataDocument document) {
        // queue an accession message if the document has no uuid
        Optional<UUID> uuidOptional = Optional.of(document)
                .map(AbstractEntity::getUuid)
                .map(Uuid::getUuid);

        if (!uuidOptional.isPresent()) {
            this.messageSender.queueAccessionMessage(Constants.Exchanges.ACCESSION,
                    Constants.Queues.ACCESSION_REQUIRED,
                    messageFor(document),
                    document.getUpdateDate().toEpochMilli());
            return true;
        } else {
            return false;
        }
    }

    /* messages to state tracker */

    public boolean routeStateTrackingUpdateMessageFor(MetadataDocument document) {
        // allow projects to be created first before submission envelope
        if(document.getSubmissionEnvelope() != null  || document.getType() != EntityType.PROJECT){
            URI documentUpdateUri = UriComponentsBuilder.newInstance()
                    .scheme(configurationService.getStateTrackerScheme())
                    .host(configurationService.getStateTrackerHost())
                    .port(configurationService.getStateTrackerPort())
                    .pathSegment(configurationService.getDocumentStatesUpdatePath())
                    .build().toUri();

            this.messageSender.queueDocumentStateUpdateMessage(documentUpdateUri,
                    documentStateUpdateMessage(document),
                    document.getUpdateDate().toEpochMilli());
        }
        return true;
    }

    public boolean routeStateTrackingUpdateMessageForEnvelopeEvent(SubmissionEnvelope envelope, SubmissionState state) {
        // TODO: call this when a user requests a state change on an envelope
        this.messageSender.queueStateTrackingMessage(Constants.Exchanges.STATE_TRACKING,
                Constants.Routing.ENVELOPE_STATE_UPDATE,
                messageFor(envelope, state),
                envelope.getUpdateDate().toEpochMilli());
        return true;
    }

    public boolean routeStateTrackingNewSubmissionEnvelope(SubmissionEnvelope envelope) {
        this.messageSender.queueStateTrackingMessage(Constants.Exchanges.STATE_TRACKING,
                Constants.Routing.ENVELOPE_CREATE,
                messageFor(envelope),
                envelope.getUpdateDate().toEpochMilli());
        return true;
    }

    public void sendManifestForExport(ExporterData exporterData) {
        messageSender.queueNewExportMessage(ASSAY_EXCHANGE, ASSAY_SUBMITTED,
                exporterData.toManifestSubmittedMessage(linkGenerator),
                System.currentTimeMillis());
    }

    public void sendExperimentForExport(ExporterData exporterData, ExportJob exportJob) {
        messageSender.queueNewExportMessage(ASSAY_EXCHANGE, EXPERIMENT_SUBMITTED,
                exporterData.toExperimentSubmittedMessage(linkGenerator, exportJob),
                System.currentTimeMillis());
    }

    /* messages to the upload/staging area manager */

    public boolean routeRequestUploadAreaCredentials(SubmissionEnvelope envelope) {
        this.messageSender.queueUploadManagerMessage(Constants.Exchanges.UPLOAD_AREA_EXCHANGE,
                Constants.Routing.UPLOAD_AREA_CREATE,
                messageFor(envelope),
                envelope.getUpdateDate().toEpochMilli());
        return true;
    }

    public boolean routeRequestUploadAreaCleanup(SubmissionEnvelope envelope) {
        this.messageSender.queueUploadManagerMessage(Constants.Exchanges.UPLOAD_AREA_EXCHANGE,
                Constants.Routing.UPLOAD_AREA_CLEANUP,
                messageFor(envelope),
                envelope.getUpdateDate().toEpochMilli());
        return true;
    }

    private MetadataDocumentMessage messageFor(MetadataDocument document) {
        return MetadataDocumentMessageBuilder.using(linkGenerator)
                .messageFor(document)
                .build();
    }

    private SubmissionEnvelopeMessage messageFor(SubmissionEnvelope envelope) {
        return SubmissionEnvelopeMessageBuilder.using(resourceMappings, config)
                .messageFor(envelope)
                .build();
    }

    private MetadataDocumentMessage documentStateUpdateMessage(MetadataDocument document) {
        if(document.getSubmissionEnvelope() == null){
            throw new RuntimeException("The metadata document should have a link to a submission envelope.");
        }

        String envelopeId = document.getSubmissionEnvelope().getId();

        return MetadataDocumentMessageBuilder.using(linkGenerator)
                .messageFor(document)
                .withEnvelopeId(envelopeId)
                .withValidationState(document.getValidationState())
                .build();
    }

    private SubmissionEnvelopeStateUpdateMessage messageFor(SubmissionEnvelope envelope, SubmissionState state) {
        SubmissionEnvelopeStateUpdateMessage message = SubmissionEnvelopeStateUpdateMessage.fromSubmissionEnvelopeMessage(messageFor(envelope));
        message.setRequestedState(state);
        return message;
    }

}

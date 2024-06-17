package org.humancellatlas.ingest.dataset;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.dataset.util.UploadAreaUtil;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DatasetService {
    @Autowired
    private final MongoTemplate mongoTemplate;
    private final @NonNull DatasetRepository datasetRepository;
    private final @NotNull BiomaterialRepository biomaterialRepository;
    private final @NonNull ProcessRepository processRepository;
    private final @NotNull ProtocolRepository protocolRepository;
    private final @NotNull FileRepository fileRepository;
    private final @NonNull MetadataCrudService metadataCrudService;
    private final @NonNull MetadataUpdateService metadataUpdateService;
    private final @NonNull DatasetEventHandler datasetEventHandler;
    private final @NotNull UploadAreaUtil uploadAreaUtil;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public Dataset register(final Dataset dataset) {
        final Dataset persistentDataset = datasetRepository.save(dataset);

        uploadAreaUtil.createDataFilesUploadArea(dataset);
        datasetEventHandler.registeredDataset(persistentDataset);

        return persistentDataset;
    }

    public Dataset update(final Dataset dataset, final ObjectNode patch) {
        final Optional<Dataset> existingDatasetOptional = datasetRepository.findById(dataset.getId());

        if (existingDatasetOptional.isEmpty()) {
            log.warn("Dataset not found with ID: {}", dataset);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found with ID: " + dataset);
        }

        final Dataset updatedDataset = metadataUpdateService.update(existingDatasetOptional.get(), patch);

        datasetEventHandler.updatedDataset(updatedDataset);

        return updatedDataset;
    }

    public void delete(final String datasetId) {
        final Optional<Dataset> deleteDatasetOptional = datasetRepository.findById(datasetId);

        if (deleteDatasetOptional.isEmpty()) {
            log.warn("Dataset not found with ID: {}", datasetId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found with ID: " + datasetId);
        }

        final Dataset deleteDataset = deleteDatasetOptional.get();

        metadataCrudService.deleteDocument(deleteDataset);
        datasetEventHandler.deletedDataset(datasetId);
    }

    public Dataset replace(final String datasetId, final Dataset updatedDataset) {
        final Optional<Dataset> existingDatasetOptional = datasetRepository.findById(datasetId);

        if (existingDatasetOptional.isEmpty()) {
            log.warn("Dataset not found with ID: {}", datasetId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found with ID: " + datasetId);
        }

        datasetRepository.save(updatedDataset);
        datasetEventHandler.updatedDataset(updatedDataset);

        return updatedDataset;
    }

    public Dataset addDatasetToSubmissionEnvelope(final SubmissionEnvelope submissionEnvelope, final Dataset dataset) {
        if (!dataset.getIsUpdate()) {
            final Dataset savedDataset = metadataCrudService.addToSubmissionEnvelopeAndSave(dataset, submissionEnvelope);

            uploadAreaUtil.createDataFilesUploadArea(savedDataset);

            return savedDataset;
        } else {
            return metadataUpdateService.acceptUpdate(dataset, submissionEnvelope);
        }
    }

    public Dataset linkDatasetSubmissionEnvelope(final SubmissionEnvelope submissionEnvelope, final Dataset dataset) {
        final String datasetId = dataset.getId();

        dataset.addToSubmissionEnvelopes(submissionEnvelope);
        datasetRepository.save(dataset);
        datasetRepository.findByUuidUuidAndIsUpdateFalse(dataset.getUuid().getUuid()).ifPresent(datasetByUuid -> {
            if (!datasetByUuid.getId().equals(datasetId)) {
                datasetByUuid.addToSubmissionEnvelopes(submissionEnvelope);
                datasetRepository.save(datasetByUuid);
            }
        });

        return dataset;
    }

    public final Dataset linkFileToDataset(final Dataset dataset, final File file) {
        final String datasetId = dataset.getId();
        final String fileId = file.getId();

        datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset: " + datasetId));
        fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File: " + fileId));

        dataset.addFile(file);

        return datasetRepository.save(dataset);
    }

    public final Dataset linkBiomaterialToDataset(final Dataset dataset, final Biomaterial biomaterial) {
        final String datasetId = dataset.getId();
        final String biomaterialId = biomaterial.getId();

        datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset: " + datasetId));
        biomaterialRepository.findById(biomaterialId)
                .orElseThrow(() -> new ResourceNotFoundException("Biomaterial: " + biomaterialId));

        dataset.addBiomaterial(biomaterial);

        return datasetRepository.save(dataset);
    }

    public final Dataset linkProtocolToDataset(final Dataset dataset, final Protocol protocol) {
        final String datasetId = dataset.getId();
        final String protocolId = protocol.getId();

        datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset: " + datasetId));
        protocolRepository.findById(protocolId)
                .orElseThrow(() -> new ResourceNotFoundException("Protocol: " + protocolId));

        dataset.addProtocol(protocol);

        return datasetRepository.save(dataset);
    }

    public final Dataset linkProcessToDataset(final Dataset dataset, final Process process) {
        final String datasetId = dataset.getId();
        final String processId = process.getId();

        datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset: " + datasetId));
        processRepository.findById(processId)
                .orElseThrow(() -> new ResourceNotFoundException("Process: " + processId));

        dataset.addProcess(process);

        return datasetRepository.save(dataset);
    }
}

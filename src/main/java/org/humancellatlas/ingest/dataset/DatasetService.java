package org.humancellatlas.ingest.dataset;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.dataset.util.UploadAreaUtil;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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

    public Dataset update(final String datasetId, final ObjectNode patch) {
        final Optional<Dataset> existingDatasetOptional = datasetRepository.findById(datasetId);

        if (existingDatasetOptional.isEmpty()) {
            log.warn("Dataset not found with ID: {}", datasetId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dataset not found with ID: " + datasetId);
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
}

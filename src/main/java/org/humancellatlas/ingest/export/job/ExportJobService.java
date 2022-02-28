package org.humancellatlas.ingest.export.job;

import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.destination.ExportDestination;
import org.humancellatlas.ingest.export.destination.ExportDestinationName;
import org.humancellatlas.ingest.export.entity.ExportEntity;
import org.humancellatlas.ingest.export.job.web.ExportJobRequest;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import static org.humancellatlas.ingest.export.destination.ExportDestinationName.DCP;

@Component
@AllArgsConstructor
public class ExportJobService {
    private final ExportJobRepository exportJobRepository;
    private final SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final MongoTemplate mongoTemplate;

    public ExportJob createExportJob(SubmissionEnvelope submissionEnvelope, ExportJobRequest exportJobRequest) {
        ExportJob newExportJob = ExportJob.builder()
                .status(ExportState.EXPORTING)
                .errors(new ArrayList<>())
                .submission(submissionEnvelope)
                .destination(exportJobRequest.getDestination())
                .context(exportJobRequest.getContext())
                .build();
        return exportJobRepository.insert(newExportJob);
    }

    public Page<ExportJob> find(UUID submissionUuid,
                                ExportState exportState,
                                ExportDestinationName destinationName,
                                String version,
                                Pageable pageable) {
        SubmissionEnvelope submissionEnvelope = submissionEnvelopeRepository.findByUuidUuid(submissionUuid);
        ExportJob exportJobProbe = ExportJob.builder()
                .submission(submissionEnvelope)
                .status(exportState)
                .destination(new ExportDestination(destinationName, version, null))
                .build();
        return this.exportJobRepository.findAll(Example.of(exportJobProbe), pageable);


    }

    public Optional<ExportJob> getLastDcpExportJobCompleted(SubmissionEnvelope submissionEnvelope) {
        PageRequest request = PageRequest.of(0, 1, new Sort(Sort.Direction.DESC, "createdDate"));
        ExportDestination exportDestination = new ExportDestination(DCP, "v2", null);
        List<ExportJob> exportJobs = exportJobRepository.findBySubmissionAndStatusAndDestination(submissionEnvelope, ExportState.EXPORTED, exportDestination, request).getContent();
        return exportJobs.stream().findFirst();
    }

    public Collection<String> getAssayProcessIds(ExportJob exportJob) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("context.exportEntityType").is("assayProcess"));
        Criteria queryCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
        Query query = new Query().addCriteria(queryCriteria);
        List<ExportEntity> entities = mongoTemplate.find(query, ExportEntity.class);
        return entities.stream()
                .map(entity -> (String) entity.getContext().get("assayProcessId"))
                .collect(Collectors.toList());
    }
}

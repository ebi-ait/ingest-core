package org.humancellatlas.ingest.export.entity;

import lombok.AllArgsConstructor;
import org.humancellatlas.ingest.export.entity.web.ExportEntityRequest;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.project.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class ExportEntityService {
    private final ExportEntityRepository exportEntityRepository;



    public ExportEntity createExportEntity(ExportJob exportJob, ExportEntityRequest exportEntityRequest) {
        ExportEntity newExportEntity = ExportEntity.builder()
                .exportJob(exportJob)
                .status(exportEntityRequest.getStatus())
                .context(exportEntityRequest.getContext())
                .errors(exportEntityRequest.getErrors())
                .build();
        return exportEntityRepository.insert(newExportEntity);
    }

}

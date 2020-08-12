package org.humancellatlas.ingest.export.entity.web;

import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.entity.ExportEntity;
import org.humancellatlas.ingest.export.entity.ExportEntityRepository;
import org.humancellatlas.ingest.export.entity.ExportEntityService;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(ExportEntity.class)
public class ExportEntityController {
    private final ExportEntityService exportEntityService;
    private final ExportEntityRepository exportEntityRepository;
    private final PagedResourcesAssembler pagedAssembler;

    @GetMapping(path = Links.EXPORT_JOBS_URL + "/{id}" + Links.EXPORT_JOB_ENTITIES_URL)
    public ResponseEntity<?> getExportJobEntities(@PathVariable("id") ExportJob exportJob,
                                                  @RequestParam(name = "status", required = false) ExportState exportState,
                                                  Pageable pageable,
                                                  PersistentEntityResourceAssembler assembler) {
        if (exportJob == null) {
            return ResponseEntity.notFound().build();
        }
        Page<ExportEntity> entityPage;
        if (exportState == null) {
            entityPage = exportEntityRepository.findByExportJob(exportJob, pageable);
        } else {
            entityPage = exportEntityRepository.findByExportJobAndStatus(exportJob, exportState, pageable);
        }
        return ResponseEntity.ok(pagedAssembler.toResource(entityPage ,assembler));
    }

    @GetMapping(path = Links.EXPORT_JOBS_URL + "/{job_id}" + Links.EXPORT_JOB_ENTITIES_URL + "/{entity_id}")
    ResponseEntity<?> getExportJobEntity(@PathVariable("job_id") ExportJob exportJob,
                                        @PathVariable("entity_id") ExportEntity exportEntity,
                                         PersistentEntityResourceAssembler assembler) {
        if (exportJob == null || exportEntity == null) {
            return ResponseEntity.notFound().build();
        }
        PersistentEntityResource newExportEntityResource = assembler.toFullResource(exportEntity);
        return ResponseEntity.ok(newExportEntityResource);
    }

    @PostMapping(path = Links.EXPORT_JOBS_URL + "/{id}" + Links.EXPORT_JOB_ENTITIES_URL)
    ResponseEntity<PersistentEntityResource> createExportEntity(@PathVariable("id") ExportJob exportJob,
                                                                @RequestBody ExportEntityRequest exportEntityRequest,
                                                                PersistentEntityResourceAssembler resourceAssembler){
        if (exportJob == null) {
            return ResponseEntity.notFound().build();
        }
        ExportEntity newExportEntity = exportEntityService.createExportEntity(exportJob, exportEntityRequest);
        PersistentEntityResource newExportEntityResource = resourceAssembler.toFullResource(newExportEntity);
        return ResponseEntity.created(URI.create(newExportEntityResource.getId().getHref())).body(newExportEntityResource);
    }
}

package org.humancellatlas.ingest.export.entity.web;

import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.export.entity.ExportEntity;
import org.humancellatlas.ingest.export.entity.ExportEntityService;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(ExportEntity.class)
public class ExportEntityController {
    private final ExportEntityService exportEntityService;

    @PostMapping(path = Links.EXPORT_JOB_URL + "/{id}/entities")
    ResponseEntity<PersistentEntityResource> createExportEntity(
        @PathVariable("id") ExportJob exportJob,
        @RequestBody ExportEntityRequest exportEntityRequest,
        final PersistentEntityResourceAssembler resourceAssembler){
        ExportEntity newExportEntity = exportEntityService.createExportEntity(exportJob, exportEntityRequest);
        PersistentEntityResource newExportEntityResource = resourceAssembler.toFullResource(newExportEntity);
        return ResponseEntity.created(URI.create(newExportEntityResource.getId().getHref())).body(newExportEntityResource);
    }
}

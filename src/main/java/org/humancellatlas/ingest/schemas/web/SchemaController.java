package org.humancellatlas.ingest.schemas.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.schemas.Schema;
import org.humancellatlas.ingest.schemas.SchemaService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by rolando on 19/04/2018.
 */
@RepositoryRestController
@ExposesResourceFor(Schema.class)
@RequiredArgsConstructor
public class SchemaController {
    private final @NonNull SchemaService schemaService;

    @RequestMapping(path = "/schemas/update", method = RequestMethod.POST)
    ResponseEntity<?> triggerSchemasUpdate() {
        schemaService.updateSchemasCollection();
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}

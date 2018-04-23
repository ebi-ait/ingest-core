package org.humancellatlas.ingest.schemas.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.schemas.Schema;
import org.humancellatlas.ingest.schemas.SchemaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by rolando on 19/04/2018.
 */
@RepositoryRestController
@ExposesResourceFor(Schema.class)
@RequiredArgsConstructor
public class SchemaController {
    private final @NonNull SchemaService schemaService;
    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;


    @RequestMapping(path = "/schemas/update", method = RequestMethod.POST)
    ResponseEntity<?> triggerSchemasUpdate() {
        schemaService.updateSchemasCollection();
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @RequestMapping(path = "/schemas/search/querySchemas", method = RequestMethod.GET)
    ResponseEntity<?> querySchemas(@RequestParam String highLevelEntity,
                                   @RequestParam String concreteEntity,
                                   @RequestParam String domainEntity,
                                   @RequestParam String subDomainEntity,
                                   @RequestParam String schemaVersion,
                                   Pageable pageable,
                                   final PersistentEntityResourceAssembler resourceAssembler) {

        Page<Schema> schemaPage = schemaService.querySchemas(highLevelEntity,
                                                             concreteEntity,
                                                             domainEntity,
                                                             subDomainEntity,
                                                             schemaVersion,
                                                             pageable);

        return ResponseEntity.ok(pagedResourcesAssembler.toResource(schemaPage, resourceAssembler));
    }
}

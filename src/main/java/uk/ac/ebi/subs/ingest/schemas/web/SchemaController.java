package uk.ac.ebi.subs.ingest.schemas.web;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.schemas.Schema;
import uk.ac.ebi.subs.ingest.schemas.SchemaService;

/** Created by rolando on 19/04/2018. */
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

  @RequestMapping(path = "/schemas/search/latestSchemas", method = RequestMethod.GET)
  ResponseEntity<?> latestSchemas(
      Pageable pageable, final PersistentEntityResourceAssembler resourceAssembler) {
    List<Schema> latestSchemas = schemaService.getLatestSchemas();
    Page<Schema> latestSchemasPage = generatePageFromSchemaList(pageable, latestSchemas);
    return ResponseEntity.ok(
        pagedResourcesAssembler.toResource(latestSchemasPage, resourceAssembler));
  }

  @RequestMapping(path = "/schemas/search/filterLatestSchemas", method = RequestMethod.GET)
  ResponseEntity<?> filterLatestSchemas(
      @RequestParam String highLevelEntity,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    List<Schema> latestSchemas = schemaService.filterLatestSchemas(highLevelEntity);
    Page<Schema> latestSchemasPage = generatePageFromSchemaList(pageable, latestSchemas);
    return ResponseEntity.ok(
        pagedResourcesAssembler.toResource(latestSchemasPage, resourceAssembler));
  }

  private Page<Schema> generatePageFromSchemaList(Pageable pageable, List<Schema> schemaList) {
    List<Schema> latestSchemasSubList =
        schemaList.subList(
            (int) (pageable.getOffset()),
            (int)
                (pageable.getOffset()
                    + Math.min(
                        pageable.getOffset() + pageable.getPageSize(),
                        schemaList.size() - pageable.getOffset())));

    return new PageImpl<>(latestSchemasSubList, pageable, schemaList.size());
  }
}

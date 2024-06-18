package org.humancellatlas.ingest.patch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.webmvc.json.DomainObjectReader;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Utility class mainly for applying patches to domain objects. */
@Component
public class JsonPatcher {

  private final DomainObjectReader domainObjectReader;

  private final ObjectMapper objectMapper;

  @Autowired
  public JsonPatcher(
      PersistentEntities persistentEntities, Associations associations, ObjectMapper objectMapper) {
    this.domainObjectReader = new DomainObjectReader(persistentEntities, associations);
    this.objectMapper = objectMapper;
  }

  /*
  Almost the same exact implementation used in {@link org.springframework.data.rest.webmvc.config.JsonPatchHandler}
  to merge JSON documents for Spring Data REST. It's copied here because the patch code that Spring uses was made
  internal to the framework.
   */
  public <T> T merge(ObjectNode patch, T target) {
    return domainObjectReader.merge(patch, target, objectMapper);
  }
}

package org.humancellatlas.ingest.schemas;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.schemas.schemascraper.SchemaScraper;
import org.humancellatlas.ingest.schemas.schemascraper.impl.SchemaScrapeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Service
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SchemaService {

  @Autowired private SchemaRepository schemaRepository;

  @Autowired private SchemaScraper schemaScraper;

  @Autowired private Environment environment;

  private static final int EVERY_24_HOURS = 1000 * 60 * 60 * 24;

  public List<Schema> filterLatestSchemas(String highLevelEntity) {
    return getLatestSchemas().stream()
        .filter(schema -> schema.getHighLevelEntity().matches(highLevelEntity))
        .collect(Collectors.toList());
  }

  public List<Schema> getLatestSchemas() {
    List<Schema> allSchemas = schemaRepository.findAll();
    allSchemas.sort(Collections.reverseOrder());

    Set<LatestSchema> latestSchemas = new LinkedHashSet<>();
    allSchemas.stream().map(LatestSchema::new).forEach(latestSchemas::add);

    return latestSchemas.stream().map(LatestSchema::getSchema).collect(Collectors.toList());
  }

  public Schema getLatestSchemaByEntityType(String highLevelEntity, String entityType) {
    List<Schema> allLatestSchema =
        filterLatestSchemas(highLevelEntity).stream()
            .filter(schema -> schema.getConcreteEntity().matches(entityType))
            .collect(Collectors.toList());

    return allLatestSchema.size() > 0 ? allLatestSchema.get(0) : null;
  }

  @Scheduled(fixedDelay = EVERY_24_HOURS)
  public void updateSchemasCollection() {
    String schemaBaseUri = getSchemaBaseUri();

    if (schemaBaseUri == null)
      throw new SchemaScrapeException("SCHEMA_BASE_URI environmental variable should not be null.");

    if (schemaBaseUri.endsWith("/")) {
      schemaBaseUri = schemaBaseUri.substring(0, schemaBaseUri.length() - 1);
    }

    // TODO Find a way how to neatly exclude the files
    schemaScraper.getAllSchemaURIs(URI.create(schemaBaseUri)).stream()
        .filter(
            schemaUri ->
                !schemaUri.toString().contains("index.html")
                    && !schemaUri.toString().contains("property_migrations"))
        .forEach(this::doUpdate);
  }

  public String getSchemaBaseUri() {
    return environment.getProperty("SCHEMA_BASE_URI");
  }

  private void doUpdate(URI schemaUri) {
    Schema schemaDocument = schemaDescriptionFromSchemaUri(schemaUri);

    UUID schemaUuid = UUID.nameUUIDFromBytes(schemaUri.toString().getBytes());
    schemaDocument.setUuid(new Uuid(schemaUuid.toString()));

    deleteMatchingSchemas(schemaUuid);
    schemaRepository.save(schemaDocument);
  }

  private void deleteMatchingSchemas(UUID schemaUuid) {
    Collection<Schema> matchingSchemas =
        schemaRepository.findByUuidEquals(new Uuid(schemaUuid.toString()));
    schemaRepository.deleteAll(matchingSchemas);
  }

  public Collection<Schema> schemaDescriptionFromSchemaUris(Collection<URI> schemaUris) {
    return schemaUris.stream()
        .map(this::schemaDescriptionFromSchemaUri)
        .collect(Collectors.toList());
  }

  private Schema schemaDescriptionFromSchemaUri(URI schemaUri) {
    String[] splitString = schemaUri.toString().split("/");
    String schemaFullUri = environment.getProperty("SCHEMA_BASE_URI") + schemaUri;

    if (splitString.length == 3) { // then this is a bundle schema
      return new Schema(splitString[0], splitString[1], "", "", splitString[2], schemaFullUri);
    } else if (splitString.length == 4) {
      return new Schema(
          splitString[0], splitString[2], splitString[1], "", splitString[3], schemaFullUri);
    } else if (splitString.length == 5) {
      return new Schema(
          splitString[0],
          splitString[3],
          splitString[1],
          splitString[2],
          splitString[4],
          schemaFullUri);
    } else {
      throw new SchemaScrapeException(
          "Couldn't construct a Schema document from URI: " + schemaFullUri);
    }
  }

  /**
   * A wrapper for Schema documents used to define a looser equals()/hashCode() to determine
   * equivalence of Schemas based only on a Schema's high level entity, type, etc.
   */
  private class LatestSchema {
    @Getter private final Schema schema;

    LatestSchema(Schema schema) {
      this.schema = schema;
    }

    @Override
    public boolean equals(Object to) {
      if (to == this) return true;
      if (!(to instanceof LatestSchema)) {
        return false;
      }

      LatestSchema schema = (LatestSchema) to;
      return schema.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
      int result = 17;
      result = 31 * result + this.schema.getConcreteEntity().hashCode();
      result = 31 * result + this.schema.getHighLevelEntity().hashCode();
      result = 31 * result + this.schema.getDomainEntity().hashCode();
      result = 31 * result + this.schema.getSubDomainEntity().hashCode();
      return result;
    }
  }
}

package uk.ac.ebi.subs.ingest.schemas.schemascraper;

import java.net.URI;
import java.util.Collection;

/**
 * Created by rolando on 19/04/2018.
 *
 * <p>Collects schemas from schema.humancellatlas.org (or some other schema bucket)
 */
public interface SchemaScraper {
  Collection<URI> getAllSchemaURIs(URI schemaBucketLocation);
}

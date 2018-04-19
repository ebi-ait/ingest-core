package org.humancellatlas.ingest.schemas.web;

import org.humancellatlas.ingest.schemas.Schema;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;

/**
 * Created by rolando on 19/04/2018.
 */
@RepositoryRestController
@ExposesResourceFor(Schema.class)
public class SchemaController {

}

package org.humancellatlas.ingest.patch.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.patch.Patch;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;

@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(Patch.class)
@Getter
public class PatchController {
}

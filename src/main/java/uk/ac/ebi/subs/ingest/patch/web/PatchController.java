package uk.ac.ebi.subs.ingest.patch.web;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.patch.Patch;

@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(Patch.class)
@Getter
public class PatchController {}

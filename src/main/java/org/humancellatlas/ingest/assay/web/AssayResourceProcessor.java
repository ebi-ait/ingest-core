package org.humancellatlas.ingest.assay.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.assay.Assay;
import org.humancellatlas.ingest.core.web.Links;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/09/17
 */
@Component
@RequiredArgsConstructor
public class AssayResourceProcessor implements ResourceProcessor<Resource<Assay>> {
    private final @NonNull EntityLinks entityLinks;

    private Link getFileReferenceLink(Assay assay) {
        return entityLinks.linkForSingleResource(assay).slash(Links.FILE_REF_URL).withRel(Links.FILE_REF_REL);
    }

    public Resource<Assay> process(Resource<Assay> assayResource) {
        Assay assay = assayResource.getContent();
        assayResource.add(getFileReferenceLink(assay));
        return assayResource;
    }
}

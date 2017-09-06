package org.humancellatlas.ingest.analysis.web;

import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.analysis.Analysis;
import org.humancellatlas.ingest.core.web.Links;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/09/17
 */
@Component
@RequiredArgsConstructor
public class AnalysisResourceProcessor implements ResourceProcessor<Resource<Analysis>> {
    private final @NotNull EntityLinks entityLinks;

    private Link getBundleReferencesLink(Analysis analysis) {
        return entityLinks.linkForSingleResource(analysis).slash(Links.BUNDLE_REF_URL).withRel(Links.BUNDLE_REF_REL);
    }

    public Resource<Analysis> process(Resource<Analysis> analysisResource) {
        analysisResource.add(getBundleReferencesLink(analysisResource.getContent()));
        return analysisResource;
    }
}




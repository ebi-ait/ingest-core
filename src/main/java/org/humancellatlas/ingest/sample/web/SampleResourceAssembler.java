package org.humancellatlas.ingest.sample.web;

import org.humancellatlas.ingest.sample.Sample;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Component;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/09/17
 */
@Component
public class SampleResourceAssembler extends ResourceAssemblerSupport<Sample, SampleResource> {
    public SampleResourceAssembler() {
        super(SampleController.class, SampleResource.class);
    }

    @Override public SampleResource toResource(Sample sample) {
        SampleResource resource = createResourceWithId(sample.getId(), sample);

    }
}

package org.humancellatlas.ingest.core.web;

import org.humancellatlas.ingest.sample.Sample;
import org.humancellatlas.ingest.sample.web.SampleController;
import org.humancellatlas.ingest.sample.web.SampleResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@Configuration
public class ResourceConfigurations {
    @Bean ResourceAssembler<Sample, SampleResource> sampleResourceAssembler() {
        return new SampleResourceAssembler(SampleController.class, SampleResource.class);
    }

    static class SampleResourceAssembler extends ResourceAssemblerSupport<Sample, SampleResource> {
        /**
         * Creates a new {@link ResourceAssemblerSupport} using the given controller class and resource type.
         *
         * @param controllerClass must not be {@literal null}.
         * @param resourceType    must not be {@literal null}.
         */
        private SampleResourceAssembler(Class<?> controllerClass,
                                        Class<SampleResource> resourceType) {
            super(controllerClass, resourceType);
        }

        @Override public SampleResource toResource(Sample sample) {
            return new SampleResource(sample);
        }
    }

}


package org.humancellatlas.ingest.sample.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.sample.Sample;
import org.springframework.hateoas.ResourceSupport;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@RequiredArgsConstructor
@Getter
public class SampleResource extends ResourceSupport {
    private final Sample sample;
}

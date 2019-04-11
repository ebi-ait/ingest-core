package org.humancellatlas.ingest.submission;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

import java.net.URI;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 15/09/17
 */
@Data
class StagingUrn {
    private URI value;

    @JsonCreator
    public StagingUrn(String name) {
        this.value = URI.create(name);

        // test this uri is a URN
        if (!value.isOpaque()) {
            throw new IllegalArgumentException(String.format("Staging URN is malformed: %s", value.toString()));
        }
    }

    StagingUrn() {

    }
}

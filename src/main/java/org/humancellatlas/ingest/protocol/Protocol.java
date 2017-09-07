package org.humancellatlas.ingest.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.core.*;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class Protocol extends BioMetadataDocument {
    protected Protocol() {
        super(EntityType.PROTOCOL, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, null, ValidationStatus.PENDING);
    }

    @JsonCreator
    public Protocol(Object content) {
        super(EntityType.PROTOCOL, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, content, ValidationStatus.PENDING);
    }
}

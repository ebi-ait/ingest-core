package org.humancellatlas.ingest.protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class Protocol extends MetadataDocument {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Protocol(Object content) {
        super(EntityType.PROTOCOL, content);
    }

}

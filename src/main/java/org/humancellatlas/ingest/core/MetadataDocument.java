package org.humancellatlas.ingest.core;

import lombok.Getter;
import lombok.Setter;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@Getter
public abstract class MetadataDocument extends AbstractEntity {
    private final Object content;

    protected MetadataDocument(EntityType type,
                               Uuid uuid,
                               SubmissionDate submissionDate,
                               UpdateDate updateDate,
                               Object content) {
        super(type, uuid, submissionDate, updateDate);

        this.content = content;
    }
}

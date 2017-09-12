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
    private final Accession accession;
    private final Object content;

    private @Setter ValidationState validationState;

    protected MetadataDocument(EntityType type,
                               Uuid uuid,
                               SubmissionDate submissionDate,
                               UpdateDate updateDate,
                               Accession accession,
                               ValidationState validationState,
                               Object content) {
        super(type, uuid, submissionDate, updateDate);

        this.accession = accession;
        this.validationState = validationState;
        this.content = content;
    }
}

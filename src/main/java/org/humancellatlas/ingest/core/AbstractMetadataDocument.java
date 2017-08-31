package org.humancellatlas.ingest.core;

import lombok.Getter;

import java.util.UUID;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 31/08/17
 */
@Getter
public abstract class AbstractMetadataDocument extends AbstractEntity {
    private final Accession accession;
    private final Object content;

    protected AbstractMetadataDocument(EntityType type,
                                       UUID uuid,
                                       SubmissionDate submissionDate,
                                       UpdateDate updateDate,
                                       Accession accession,
                                       Object content) {
        super(type, uuid, submissionDate, updateDate);

        this.accession = accession;
        this.content = content;
    }
}

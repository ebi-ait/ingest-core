package org.humancellatlas.ingest.submission;

import lombok.Getter;
import org.humancellatlas.ingest.core.AbstractMetadataDocument;
import org.humancellatlas.ingest.core.Accession;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.SubmissionDate;
import org.humancellatlas.ingest.core.UpdateDate;

import java.util.UUID;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class Protocol extends AbstractMetadataDocument {
    protected Protocol(UUID uuid,
                       SubmissionDate submissionDate,
                       UpdateDate updateDate,
                       Accession accession,
                       Object content) {
        super(EntityType.PROTOCOL, uuid, submissionDate, updateDate, accession, content);
    }

}

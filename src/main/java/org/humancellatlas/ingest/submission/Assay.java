package org.humancellatlas.ingest.submission;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.Accession;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.SubmissionDate;
import org.humancellatlas.ingest.core.UpdateDate;
import org.humancellatlas.ingest.core.Uuid;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class Assay extends MetadataDocument {
    protected Assay() {
        super(EntityType.ASSAY, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, null);
    }

    @JsonCreator
    public Assay(Object content) {
        super(EntityType.ASSAY, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, content);
    }
}

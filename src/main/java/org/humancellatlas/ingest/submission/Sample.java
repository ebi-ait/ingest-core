package org.humancellatlas.ingest.submission;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.SubmissionDate;
import org.humancellatlas.ingest.core.UpdateDate;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class Sample extends MetadataDocument {
    protected Sample() {
        super(EntityType.SAMPLE, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, null);
    }

    @JsonCreator
    public Sample(Object content) {
        super(EntityType.SAMPLE, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, content);
    }
}

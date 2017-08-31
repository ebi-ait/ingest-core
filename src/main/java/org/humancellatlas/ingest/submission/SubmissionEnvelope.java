package org.humancellatlas.ingest.submission;

import lombok.Getter;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.SubmissionDate;
import org.humancellatlas.ingest.core.UpdateDate;
import org.humancellatlas.ingest.core.Uuid;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class SubmissionEnvelope extends AbstractEntity {
    public SubmissionEnvelope(Uuid uuid, SubmissionDate submissionDate, UpdateDate updateDate) {
        super(EntityType.SUBMISSION, uuid, submissionDate, updateDate);
    }

    public SubmissionEnvelope() {
        this(null, null, null);
    }
}

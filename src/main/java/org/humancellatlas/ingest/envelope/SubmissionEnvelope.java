package org.humancellatlas.ingest.envelope;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.humancellatlas.ingest.analysis.Analysis;
import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.assay.Assay;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.sample.Sample;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class SubmissionEnvelope extends AbstractEntity {
    private @Setter SubmissionStatus submissionStatus;

    public SubmissionEnvelope(Uuid uuid,
                              SubmissionDate submissionDate,
                              UpdateDate updateDate,
                              SubmissionStatus submissionStatus) {
        super(EntityType.SUBMISSION, uuid, submissionDate, updateDate);
        this.submissionStatus = submissionStatus;
    }

    public SubmissionEnvelope() {
        this(null,
             new SubmissionDate(new Date()),
             new UpdateDate(new Date()),
             SubmissionStatus.DRAFT);
    }
}

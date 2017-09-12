package org.humancellatlas.ingest.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.core.Accession;
import org.humancellatlas.ingest.core.Checksums;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.SubmissionDate;
import org.humancellatlas.ingest.core.UpdateDate;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Date;

@Getter
@Setter
public class File extends MetadataDocument {
    private final String fileName;

    private String cloudUrl;
    private Checksums checksums;

    private @DBRef SubmissionEnvelope submissionEnvelope;

    protected File() {
        super(EntityType.FILE, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, ValidationState.PENDING, null);
        this.submissionEnvelope = null;
        this.cloudUrl = "";
        this.fileName = "";
        this.checksums = null;
    }

    protected File(EntityType type,
                   Uuid uuid,
                   SubmissionDate submissionDate,
                   UpdateDate updateDate,
                   Accession accession,
                   ValidationState validationState,
                   String fileName,
                   String cloudUrl,
                   Checksums checksums,
                   SubmissionEnvelope submissionEnvelope,
                   Object content) {
        super(type, uuid, submissionDate, updateDate, accession, validationState, content);
        this.fileName = fileName;
        this.cloudUrl = cloudUrl;
        this.checksums = checksums;
        this.submissionEnvelope = submissionEnvelope;
    }

    @JsonCreator
    protected File(@JsonProperty("fileName") String fileName,
                   @JsonProperty("content") Object content) {
        this(EntityType.FILE,
             null,
             new SubmissionDate(new Date()),
             new UpdateDate(new Date()),
             null,
             ValidationState.PENDING,
             fileName,
             "",
             null,
             null,
             content);
    }

    public File addToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope) {
        this.submissionEnvelope = submissionEnvelope;

        return this;
    }

    public boolean isInEnvelope(SubmissionEnvelope submissionEnvelope) {
        return this.submissionEnvelope.equals(submissionEnvelope);
    }

    public boolean isInEnvelopeWithUuid(Uuid uuid) {
        return this.submissionEnvelope.getUuid().equals(uuid);
    }
}

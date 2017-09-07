package org.humancellatlas.ingest.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.core.*;

import java.util.Date;

@Getter
@Setter
public class File extends MetadataDocument {
    private String fileName;
    private String cloudUrl;
    private Checksums checksums;

    protected File() {
        super(EntityType.FILE, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null);
        this.cloudUrl = "";
        this.fileName = "";
        this.checksums = null;
    }

    protected File(EntityType type,
                   Uuid uuid,
                   SubmissionDate submissionDate,
                   UpdateDate updateDate,
                   String fileName,
                   String cloudUrl,
                   Checksums checksums,
                   Object content) {
        super(type, uuid, submissionDate, updateDate, content);
        this.fileName = fileName;
        this.cloudUrl = cloudUrl;
        this.checksums = checksums;
    }

    @JsonCreator
    protected File(@JsonProperty("fileName") String fileName,
                   @JsonProperty("content") Object content) {
        this(EntityType.FILE,
             null,
             new SubmissionDate(new Date()),
             new UpdateDate(new Date()),
             fileName,
             "",
             null,
             content);
    }
}

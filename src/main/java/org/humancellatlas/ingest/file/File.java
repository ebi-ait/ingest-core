package org.humancellatlas.ingest.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.core.*;

import java.util.Date;

@Getter
public class File extends MetadataDocument{
    private String fileName;
    private String cloudUrl;
    private Checksums checksums;

    protected File(){
        super(EntityType.FILE, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, null);
        this.cloudUrl = "";
        this.fileName = "";
        this.checksums = null;
    }

    protected File(EntityType type,
                 Uuid uuid,
                 SubmissionDate submissionDate,
                 UpdateDate updateDate,
                 Accession accession,
                 String fileName,
                 String cloudUrl,
                 Checksums checksums,
                 Object content) {
        super(type, uuid, submissionDate, updateDate, accession, content);
        this.fileName = fileName;
        this.cloudUrl = cloudUrl;
        this.checksums = checksums;
    }

    @JsonCreator
    protected File(Object content) {
        this(EntityType.FILE,
                null,
                new SubmissionDate(new Date()),
                new UpdateDate(new Date()),
                null,
                "",
                "",
                null,
                content);
    }
}

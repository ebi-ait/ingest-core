package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.core.*;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
public class Project extends BioMetadataDocument {
    protected Project() {
        super(EntityType.PROJECT, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, null, ValidationStatus.PENDING);
    }

    @JsonCreator
    public Project(Object content) {
        super(EntityType.PROJECT, null, new SubmissionDate(new Date()), new UpdateDate(new Date()), null, content, ValidationStatus.PENDING);
    }
}
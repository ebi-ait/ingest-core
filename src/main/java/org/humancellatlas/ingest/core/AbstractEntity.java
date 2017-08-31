package org.humancellatlas.ingest.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.hateoas.Identifiable;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
@ToString
@EqualsAndHashCode
public abstract class AbstractEntity implements Identifiable<String> {
    private @Id @JsonIgnore String id;

    private @JsonIgnore final EntityType type;

    private final Uuid uuid;
    private final SubmissionDate submissionDate;
    private final UpdateDate updateDate;

    protected AbstractEntity(EntityType type,
                             Uuid uuid,
                             SubmissionDate submissionDate,
                             UpdateDate updateDate) {
        this.type = type;
        this.uuid = uuid;
        this.submissionDate = submissionDate;
        this.updateDate = updateDate;
    }
}

package org.humancellatlas.ingest.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.hateoas.Identifiable;

import java.util.Date;

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

    private final @JsonIgnore EntityType type;

    private final SubmissionDate submissionDate;

    private Uuid uuid;
    private UpdateDate updateDate;

    protected AbstractEntity(EntityType type) {
        this.type = type;
        this.submissionDate = new SubmissionDate(new Date());
        update();
    }

    public void update() {
        this.updateDate = new UpdateDate(new Date());
    }
}

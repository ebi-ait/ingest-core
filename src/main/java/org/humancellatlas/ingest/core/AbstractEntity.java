package org.humancellatlas.ingest.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;
import org.springframework.data.annotation.*;
import org.springframework.hateoas.Identifiable;

import java.time.Instant;
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

    private @Version Long version;

    private @CreatedDate DateTime submissionDate;

    private @LastModifiedDate DateTime updateDate;

    private @CreatedBy String user;

    private @LastModifiedBy String lastModifiedUser;

    private final @JsonIgnore EntityType type;

    private @Setter Uuid uuid;

    protected AbstractEntity(EntityType type) {
        this.type = type;
    }
}

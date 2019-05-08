package org.humancellatlas.ingest.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;
import org.springframework.data.annotation.*;
import org.springframework.hateoas.Identifiable;

import java.util.ArrayList;
import java.util.List;

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
    protected  @Id @JsonIgnore String id;

    private @Version Long version;

    private @CreatedDate DateTime submissionDate;

    private @LastModifiedDate DateTime updateDate;

    private @CreatedBy String user;

    private @LastModifiedBy String lastModifiedUser;

    private @JsonIgnore EntityType type;

    private @Setter Uuid uuid;

    private @Setter List<Event> events = new ArrayList<>();

    private @Setter DateTime dcpVersion;

    protected AbstractEntity(EntityType type) {
        this.type = type;
    }

    protected AbstractEntity() {}

}

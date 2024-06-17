package org.humancellatlas.ingest.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.*;
import org.springframework.hateoas.Identifiable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 30/08/17
 */
@Getter
@ToString
@JsonIgnoreProperties(
    value = {"type"},
    allowGetters = true)
@EqualsAndHashCode
public abstract class AbstractEntity implements Identifiable<String> {
  protected @Id @JsonIgnore String id;

  private @Version Long version;

  private @CreatedDate Instant submissionDate;

  private @LastModifiedDate Instant updateDate;

  private @CreatedBy String user;

  private @LastModifiedBy String lastModifiedUser;

  private EntityType type;

  private @Setter Uuid uuid;

  private @Setter List<Event> events = new ArrayList<>();

  protected AbstractEntity(EntityType type) {
    this.type = type;
  }

  protected AbstractEntity() {}
}

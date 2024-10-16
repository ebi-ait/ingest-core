package uk.ac.ebi.subs.ingest.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.*;
import org.springframework.hateoas.Identifiable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

  // This alias is used to ensure the 'id' field is included in JSON responses
  // for compatibility with morphic API clients.
  @JsonProperty("id")
  private String jsonIdAlias;

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

  public String getJsonIdAlias() {
    return id;
  }
}

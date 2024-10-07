package uk.ac.ebi.subs.ingest.protocol;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ebi.subs.ingest.core.EntityType;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.project.Project;

@Getter
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"project"})
@NoArgsConstructor
public class Protocol extends MetadataDocument {
  @Indexed
  private @Setter @DBRef(lazy = true) Project project;

  private boolean linked = false;

  @JsonCreator
  public Protocol(@JsonProperty("content") Object content) {
    super(EntityType.PROTOCOL, content);
  }

  public boolean isLinked() {
    return linked;
  }

  /* TODO
  This method was originally made as simple as possible to only support the orphaned entity use case. However,
  this can be enhanced further to add a full-fledged component that enables back linking to all Processes that refer
  to this Protocol. In that case, the isLinked implementation will need to be changed to check if the list of
  processes is empty or not. This approach was not initially chosen because it would have required data migration.
   */
  public void markAsLinked() {
    this.linked = true;
  }
}

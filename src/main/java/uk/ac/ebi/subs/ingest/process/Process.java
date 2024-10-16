package uk.ac.ebi.subs.ingest.process;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.subs.ingest.bundle.BundleManifest;
import uk.ac.ebi.subs.ingest.core.EntityType;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.protocol.Protocol;

/** Created by rolando on 16/02/2018. */
@Getter
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"project", "projects", "protocols", "inputBundleManifests", "chainedProcesses"})
public class Process extends MetadataDocument {
  @Indexed
  private @Setter @DBRef(lazy = true) Project project;

  @RestResource
  @DBRef(lazy = true)
  private Set<Project> projects = new HashSet<>();

  @RestResource
  @DBRef(lazy = true)
  private Set<Protocol> protocols = new HashSet<>();

  @RestResource
  @DBRef(lazy = true)
  @Indexed
  private Set<BundleManifest> inputBundleManifests = new HashSet<>();

  private @DBRef Set<Process> chainedProcesses = new HashSet<>();

  @JsonCreator
  public Process(@JsonProperty("content") Object content) {
    super(EntityType.PROCESS, content);
  }

  public Process addInputBundleManifest(BundleManifest bundleManifest) {
    this.inputBundleManifests.add(bundleManifest);
    return this;
  }

  public Process addProtocol(Protocol protocol) {
    protocols.add(protocol);
    return this;
  }

  public Process removeProtocol(Protocol protocol) {
    protocols.remove(protocol);
    return this;
  }
}

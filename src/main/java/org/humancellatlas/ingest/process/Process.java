package org.humancellatlas.ingest.process;

import java.util.HashSet;
import java.util.Set;

import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.dataset.Dataset;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.protocol.Protocol;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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
  private Set<Dataset> datasets = new HashSet<>();

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

  public Process addDataset(final Dataset dataset) {
    datasets.add(dataset);
    return this;
  }
}

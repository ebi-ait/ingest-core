package uk.ac.ebi.subs.ingest.dataset;

import java.util.*;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.subs.ingest.core.EntityType;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.protocol.Protocol;

@Getter
@JsonIgnoreProperties({
  "firstDcpVersion",
  "dcpVersion",
  "validationState",
  "validationErrors",
  "graphValidationErrors",
  "isUpdate"
})
public class Dataset extends MetadataDocument {
  private Set<String> dataFiles = new HashSet<>();

  private Set<String> biomaterials = new HashSet<>();

  private Set<Protocol> protocols = new HashSet<>();

  private Set<String> processes = new HashSet<>();

  @Setter private String comment;

  @JsonCreator
  public Dataset(@JsonProperty("content") final Object content) {
    super(EntityType.DATASET, content);
  }

  public void addBiomaterial(@NotNull final String biomaterialId) {
    this.biomaterials.add(biomaterialId);
  }

  public void addProtocol(@NotNull final Protocol protocol) {
    this.protocols.add(protocol);
  }

  public void addProcess(@NotNull final String processId) {
    this.processes.add(processId);
  }

  public void addFile(@NotNull final String dataFileId) {
    this.dataFiles.add(dataFileId);
  }
}

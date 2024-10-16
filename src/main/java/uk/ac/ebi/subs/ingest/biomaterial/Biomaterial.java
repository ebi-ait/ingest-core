package uk.ac.ebi.subs.ingest.biomaterial;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ebi.subs.ingest.core.EntityType;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.project.Project;

/** Created by rolando on 16/02/2018. */
@CrossOrigin
@Getter
@Document
@NoArgsConstructor
public class Biomaterial extends MetadataDocument {
  @Indexed
  @Setter
  @DBRef(lazy = true)
  private Project project;

  @RestResource
  @DBRef(lazy = true)
  private Set<Project> projects = new HashSet<>();

  @Indexed
  @RestResource
  @DBRef(lazy = true)
  private Set<Process> inputToProcesses = new HashSet<>();

  @Indexed
  @RestResource
  @DBRef(lazy = true)
  private Set<Process> derivedByProcesses = new HashSet<>();

  @Indexed
  @RestResource
  @DBRef(lazy = true)
  private Set<Biomaterial> parentBiomaterials = new HashSet<>();

  @Indexed
  @RestResource
  @DBRef(lazy = true)
  private Set<Biomaterial> childBiomaterials = new HashSet<>();

  @JsonCreator
  public Biomaterial(@JsonProperty("content") Object content) {
    super(EntityType.BIOMATERIAL, content);
  }

  /**
   * Adds to the collection of processes that this biomaterial serves as an input to
   *
   * @param process the process to add
   * @return a reference to this biomaterial
   */
  public Biomaterial addAsInputToProcess(final Process process) {
    this.inputToProcesses.add(process);
    return this;
  }

  /**
   * Adds to the collection of processes that this biomaterial was derived by
   *
   * @param process the process to add
   * @return a reference to this biomaterial
   */
  public Biomaterial addAsDerivedByProcess(final Process process) {
    this.derivedByProcesses.add(process);
    return this;
  }

  @JsonProperty(access = READ_ONLY)
  public boolean isLinked() {
    return !inputToProcesses.isEmpty() || !derivedByProcesses.isEmpty();
  }

  /**
   * Removes a process from the collection of processes that this biomaterial serves as an input to
   *
   * @param process the process to remove
   * @return a reference to this biomaterial
   */
  public Biomaterial removeAsInputToProcess(final Process process) {
    this.inputToProcesses.remove(process);
    return this;
  }

  /**
   * Removes a process from the collection of processes that this biomaterial was derived by
   *
   * @param process the process to remove
   * @return a reference to this biomaterial
   */
  public Biomaterial removeAsDerivedByProcess(final Process process) {
    this.derivedByProcesses.remove(process);
    return this;
  }

  /**
   * Adds a child biomaterial and sets this biomaterial as the parent of the child
   *
   * @param childBiomaterial the child biomaterial to add
   * @return a reference to this biomaterial
   */
  public Biomaterial addChildBiomaterial(final Biomaterial childBiomaterial) {
    childBiomaterial.addParentBiomaterial(this);
    this.childBiomaterials.add(childBiomaterial);
    return this;
  }

  /**
   * Removes a child biomaterial and clears this biomaterial as the parent of the child
   *
   * @param childBiomaterial the child biomaterial to remove
   * @return a reference to this biomaterial
   */
  public Biomaterial removeChildBiomaterial(final Biomaterial childBiomaterial) {
    childBiomaterial.removeParentBiomaterial(this);
    this.childBiomaterials.remove(childBiomaterial);
    return this;
  }

  /**
   * Adds a parent biomaterial
   *
   * @param parentBiomaterial the parent biomaterial to add
   * @return a reference to this biomaterial
   */
  public Biomaterial addParentBiomaterial(final Biomaterial parentBiomaterial) {
    this.parentBiomaterials.add(parentBiomaterial);
    return this;
  }

  /**
   * Removes a parent biomaterial
   *
   * @param parentBiomaterial the parent biomaterial to remove
   * @return a reference to this biomaterial
   */
  public Biomaterial removeParentBiomaterial(final Biomaterial parentBiomaterial) {
    this.parentBiomaterials.remove(parentBiomaterial);
    return this;
  }
}

package org.humancellatlas.ingest.biomaterial;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.project.Project;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * Created by rolando on 16/02/2018.
 */
@CrossOrigin
@Getter
@Document
@EqualsAndHashCode(callSuper = true)
public class Biomaterial extends MetadataDocument {

    @RestResource @DBRef(lazy = true) private Set<Project> projects = new HashSet<>();

    @Indexed
    @RestResource @DBRef(lazy = true) private Set<Process> inputToProcesses = new HashSet<>();

    @Indexed
    @RestResource @DBRef(lazy = true) private  Set<Process> derivedByProcesses = new HashSet<>();

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Biomaterial(Object content) {
        super(EntityType.BIOMATERIAL, content);
    }

    /**
     * Adds to the collection of processes that this biomaterial serves as an input to
     *
     * @param process the process to add
     * @return a reference to this biomaterial
     */
    public Biomaterial addAsInputToProcess(Process process) {
        this.inputToProcesses.add(process);

        return this;
    }

    /**
     * Adds to the collection of processes that this biomaterial was derived by
     *
     * @param process the process to add
     * @return a reference to this biomaterial
     */
    public Biomaterial addAsDerivedByProcess(Process process) {
        this.derivedByProcesses.add(process);

        return this;
    }

    @JsonProperty(access=READ_ONLY)
    public boolean isLinked() {
        return !inputToProcesses.isEmpty() || !derivedByProcesses.isEmpty();
    }
    /**
     * Removes a process to the collection of processes that this biomaterial serves as an input to
     *
     * @param process the process to add
     * @return a reference to this biomaterial
     */
    public Biomaterial removeAsInputToProcess(Process process) {
        this.inputToProcesses.remove(process);
        return this;
    }

    /**
     * Removes a process to the collection of processes that this biomaterial was derived by
     *
     * @param process the process to add
     * @return a reference to this biomaterial
     */
    public Biomaterial removeAsDerivedByProcess(Process process) {
        this.derivedByProcesses.remove(process);
        return this;
    }

}

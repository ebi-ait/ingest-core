package org.humancellatlas.ingest.biomaterial;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
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
import java.util.List;

/**
 * Created by rolando on 16/02/2018.
 */
@CrossOrigin
@Getter
@Document
public class Biomaterial extends MetadataDocument {

    @RestResource @DBRef private final List<Project> projects = new ArrayList<>();

    @Indexed
    @RestResource @DBRef(lazy = true) private final List<Process> inputToProcesses = new ArrayList<>();

    @Indexed
    @RestResource @DBRef(lazy = true) private final List<Process> derivedByProcesses = new ArrayList<>();

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

}

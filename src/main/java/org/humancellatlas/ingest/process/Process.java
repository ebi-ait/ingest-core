package org.humancellatlas.ingest.process;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.protocol.Protocol;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by rolando on 16/02/2018.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class Process extends MetadataDocument {

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

    public Process() {}

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public Process(Object content) {
        super(EntityType.PROCESS, content);
    }

    public Process(String id) {
        this.id = id;
    }

    public Process addInputBundleManifest(BundleManifest bundleManifest) {
        this.inputBundleManifests.add(bundleManifest);

        return this;
    }

}
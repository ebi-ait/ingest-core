package org.humancellatlas.ingest.process;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.protocol.Protocol;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rolando on 16/02/2018.
 */
@Getter
public class Process extends MetadataDocument {

    @RestResource
    @DBRef
    private final List<Project> projects = new ArrayList<>();
    @RestResource
    @DBRef
    private final List<Protocol> protocols = new ArrayList<>();
    @RestResource
    @DBRef
    private final List<BundleManifest> inputBundleManifests = new ArrayList<>();

    private final @DBRef List<Process> chainedProcesses = new ArrayList<>();

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
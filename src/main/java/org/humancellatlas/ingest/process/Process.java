package org.humancellatlas.ingest.process;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.file.File;
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
    private final List<Biomaterial> inputBiomaterials = new ArrayList<>();
    @RestResource
    @DBRef
    private final List<Biomaterial> derivedBiomaterials = new ArrayList<>();
    @RestResource
    @DBRef
    private final List<BundleManifest> inputBundleManifests = new ArrayList<>();
    @RestResource
    @DBRef
    private final List<File> inputFiles = new ArrayList<>();
    @RestResource
    @DBRef
    private final List<File> derivedFiles = new ArrayList<>();

    @JsonCreator
    public Process(Object content) {
        super(EntityType.PROCESS, content);
    }

    public Process() {}

    @JsonIgnore
    public boolean isAssaying() {
        return !inputBiomaterials.isEmpty() && !derivedFiles.isEmpty();
    }

    @JsonIgnore
    public boolean isAnalysis() {
        return !inputFiles.isEmpty() && !derivedFiles.isEmpty();
    }

    public void addInput(Biomaterial biomaterial) {
        inputBiomaterials.add(biomaterial);
    }

    public void addInput(File file) {
        inputFiles.add(file);
    }

    public void addDerivative(File file) {
        derivedFiles.add(file);
    }

}
package org.humancellatlas.ingest.dataset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@JsonIgnoreProperties({"firstDcpVersion", "dcpVersion", "validationState",
        "validationErrors", "graphValidationErrors", "isUpdate"})
public class Dataset extends MetadataDocument {
    // non-final always
    @JsonIgnore
    @DBRef(lazy = true)
    private Set<SubmissionEnvelope> submissionEnvelopes = new HashSet<>();
    private List<File> dataFiles = new ArrayList<>();
    private List<Biomaterial> biomaterials = new ArrayList<>();
    private List<Protocol> protocols = new ArrayList<>();
    private List<Process> processes = new ArrayList<>();

    @Setter
    private String comment;

    @JsonCreator
    public Dataset(@JsonProperty("content") final Object content) {
        super(EntityType.DATASET, content);
    }

    public void addToSubmissionEnvelopes(@NotNull final SubmissionEnvelope submissionEnvelope) {
        this.submissionEnvelopes.add(submissionEnvelope);
    }

    public void addBiomaterial(@NotNull final Biomaterial biomaterial) {
        this.biomaterials.add(biomaterial);
    }

    public void addProtocol(@NotNull final Protocol protocol) {
        this.protocols.add(protocol);
    }

    public void addProcess(@NotNull final Process process) {
        this.processes.add(process);
    }

    public void addFile(@NotNull final File dataFile) {
        this.dataFiles.add(dataFile);
    }

    @JsonIgnore
    public List<SubmissionEnvelope> getOpenSubmissionEnvelopes() {
        return this.submissionEnvelopes.stream()
                .filter(Objects::nonNull)
                .filter(env -> env.getSubmissionState() != null)
                .filter(SubmissionEnvelope::isOpen)
                .collect(Collectors.toList());
    }

    public Boolean getHasOpenSubmission() {
        return !getOpenSubmissionEnvelopes().isEmpty();
    }

    @JsonIgnore
    public Boolean isEditable() {
        return this.submissionEnvelopes.stream()
                .filter(Objects::nonNull)
                .allMatch(SubmissionEnvelope::isEditable);
    }
}

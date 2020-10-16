package org.humancellatlas.ingest.file;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.Checksums;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Setter
@Document
@CompoundIndexes({
        @CompoundIndex(name = "validationId", def = "{ 'validationJob.validationId': 1 }")
})
@EqualsAndHashCode(callSuper = true)
public class File extends MetadataDocument {

    @Indexed
    @RestResource @DBRef(lazy = true) private Set<Process> inputToProcesses = new HashSet<>();

    @Indexed
    @RestResource @DBRef(lazy = true) private Set<Process> derivedByProcesses = new HashSet<>();

    @Indexed
    private String fileName;
    private String cloudUrl;
    private Checksums checksums;
    private ValidationJob validationJob;
    private UUID validationId;
    private UUID dataFileUuid;
    private Long size;
    private String fileContentType;

    public File(){
        super(EntityType.FILE, null);
        setDataFileUuid(UUID.randomUUID());
    }

    @JsonCreator
    public File(@JsonProperty("content") Object content) {
        super(EntityType.FILE, content);
    }

    /**
     * Adds to the collection of processes that this file serves as an input to
     *
     * @param process the process to add
     * @return a reference to this file
     */
    public File addAsInputToProcess(Process process) {
        this.inputToProcesses.add(process);

        return this;
    }

    /**
     * Adds to the collection of processes that this file was derived by
     *
     * @param process the process to add
     * @return a reference to this file
     */
    public File addAsDerivedByProcess(Process process) {

    	// XXX why we implementing this check here but not above??
        String processId = process.getId();
        boolean processInList = derivedByProcesses.stream()
                .map(Process::getId)
                .anyMatch(id -> id.equals(processId));
        if (!processInList) {
            this.derivedByProcesses.add(process);
        }
        return this;
    }

    public void addToAnalysis(Process analysis) {
        //TODO check if this File and the Analysis belong to the same Submission?
        SubmissionEnvelope submissionEnvelope = analysis.getSubmissionEnvelope();
        super.setSubmissionEnvelope(submissionEnvelope);
        addAsDerivedByProcess(analysis);
    }

    @JsonProperty(access=READ_ONLY)
    public boolean isLinked() {
        return !inputToProcesses.isEmpty() || !derivedByProcesses.isEmpty();
    }

    /**
     * Removes a process to the collection of processes that this file was derived by
     *
     * @param process the process to add
     * @return a reference to this file
     */
    public File removeAsDerivedByProcess(Process process) {
        this.derivedByProcesses.remove(process);
        return this;
    }
    

}

package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Created by rolando on 16/02/2018.
 */
public interface ProcessRepository extends MongoRepository<Process, String> {

    Page<Process> findBySubmissionEnvelopesContaining(SubmissionEnvelope submissionEnvelope, Pageable pageable);

    Page<Process> findByInputBiomaterials(Biomaterial biomaterial, Pageable pageable);

    Page<Process> findByDerivedBiomaterials(Biomaterial biomaterial, Pageable pageable);

    Page<Process> findByInputFiles(File file, Pageable pageable);

    Page<Process> findByDerivedFiles(File file, Pageable pageable);

    //TODO find assaying Processes:
    /*
    Sample query:
    { inputBiomaterials: {
        $exists: true,
        $ne: []
      },
      defivedFiles: {
        $exists: true,
        $ne: []
      }
    }
    see [https://stackoverflow.com/a/25142571/404604] for more ideas
    */

  public Page<Process> findBySubmissionEnvelopesContainingAndValidationState(SubmissionEnvelope submissionEnvelope, ValidationState state, Pageable pageable);

}

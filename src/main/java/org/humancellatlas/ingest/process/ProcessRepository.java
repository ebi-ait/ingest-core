package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

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
}

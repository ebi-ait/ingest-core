package org.humancellatlas.ingest.process;

import java.util.List;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.file.File;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by rolando on 16/02/2018.
 */
public interface ProcessRepository extends MongoRepository<Process, String> {
  public List<Process> findByInputBiomaterialsContaining(Biomaterial biomaterial);

  public List<Process> findByInputProcess(Process process);

  public List<Process> findByInputFilesContaining(File file);
}

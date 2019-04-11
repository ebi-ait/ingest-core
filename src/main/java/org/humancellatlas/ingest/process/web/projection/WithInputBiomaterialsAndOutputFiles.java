package org.humancellatlas.ingest.process.web.projection;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.process.Process;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

/**
 * Created by rolando on 19/02/2018.
 */
@Projection(name = "withInputBiomaterialsAndOutputFiles", types = {Process.class})
public interface WithInputBiomaterialsAndOutputFiles {
  @Value("#{@biomaterialRepository.findByInputToProcessesContains(target)}")
  List<Biomaterial> getInputBiomaterials();

  @Value("#{@fileRepository.findByDerivedByProcessesContains(target)}")
  List<File> getOutputFiles();

  @Value("#{target}")
  @JsonUnwrapped Process getProcess();
}

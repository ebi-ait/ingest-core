package org.humancellatlas.ingest.process.web.projection;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.List;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.process.Process;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

/**
 * Created by rolando on 19/02/2018.
 */
@Projection(name = "withOutputFiles", types = {Process.class})
public interface WithOutputFiles {
  @Value("#{@fileRepository.findByProvenantProcesses(target)}")
  List<File> getOutputFiles();

  @Value("#{target}")
  @JsonUnwrapped Process getProcess();
}

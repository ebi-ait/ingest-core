package org.humancellatlas.ingest.process.web.projection;

import java.util.List;
import org.humancellatlas.ingest.process.Process;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

/**
 * Created by rolando on 19/02/2018.
 */
@Projection(name = "withDependingProcesses", types = {Process.class})
public interface WithDependingProcesses {
  @Value("#{@processRepository.findByInputProcess(target)}")
  List<Process> getDependingProcesses();
}

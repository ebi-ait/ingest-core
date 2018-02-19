package org.humancellatlas.ingest.biomaterial.web.projection;

import java.util.List;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.process.Process;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

/**
 * Created by rolando on 19/02/2018.
 */
@Projection(name = "destinationProcesses", types = {Biomaterial.class})
public interface WithDestinationProcesses {
  @Value("#{@processRepository.findByInputBiomaterialsContaining(target)}")
  List<Process> getDestinationProcesses();
}

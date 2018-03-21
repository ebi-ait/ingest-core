package org.humancellatlas.ingest.process.web.projection;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.process.Process;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

/**
 * Created by rolando on 20/03/2018.
 */
@Projection(name = "withInputBiomaterials", types = {Process.class})
public interface WithInputBiomaterials {
    @Value("#{@biomaterialRepository.findByInputToProcessesContains(target)}")
    List<Biomaterial> getInputBiomaterials();

    @Value("#{target}")
    @JsonUnwrapped
    Process getProcess();
}

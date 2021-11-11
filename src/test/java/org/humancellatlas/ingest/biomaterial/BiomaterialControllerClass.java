package org.humancellatlas.ingest.biomaterial;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.state.ValidationState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class BiomaterialControllerClass {

    @MockBean
    ValidationStateChangeService validationStateChangeService;

    @Autowired
    private MockMvc webApp;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private BiomaterialRepository biomaterialRepository;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @MockBean
    private MessageRouter messageRouter;

    @Test
    public void testDeleteInputToProcessTriggersValidationStateToDraft() throws Exception {
        // given
        Process process = new Process("process");
        processRepository.save(process);

        // and
        Biomaterial biomaterial = new Biomaterial(null);
        biomaterial.addAsInputToProcess(process);
        biomaterialRepository.save(biomaterial);

        // send delete request
        webApp.perform(delete("/biomaterials/{biomaterialId}/inputToProcesses/{processId}", biomaterial.getId(), process.getId()));
        // verify service being called
        verify(validationStateChangeService, times(1)).changeValidationState(any(),any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testSaveInputToProcessTriggersValidationStateToDraft() throws Exception {
        // given
        Process process = new Process("process1");
        processRepository.save(process);

        // and
        Biomaterial biomaterial = new Biomaterial(null);
        biomaterialRepository.save(biomaterial);

        // send post request
        webApp.perform(post("/biomaterials/{biomaterialId}/inputToProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()));
        // verify service being called
        verify(validationStateChangeService, times(1)).changeValidationState(any(),any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testDeleteDerivedByProcessTriggersValidationStateToDraft() throws Exception {
        // given
        Process process = new Process("process2");
        processRepository.save(process);

        // and
        Biomaterial biomaterial = new Biomaterial(null);
        biomaterial.addAsDerivedByProcess(process);
        biomaterialRepository.save(biomaterial);

        // send delete request
        webApp.perform(delete("/biomaterials/{biomaterialId}/derivedByProcesses/{processId}", biomaterial.getId(), process.getId()));
        // verify service being called
        verify(validationStateChangeService, times(1)).changeValidationState(any(),any(), eq(ValidationState.DRAFT));
    }

    @Test
    public void testDerivedByProcessTriggersValidationStateToDraft() throws Exception {
        // given
        Process process = new Process("process3");
        processRepository.save(process);

        // and
        Biomaterial biomaterial = new Biomaterial(null);
        biomaterialRepository.save(biomaterial);

        // send post request
        webApp.perform(post("/biomaterials/{biomaterialId}/derivedByProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()));
        // verify service being called
        verify(validationStateChangeService, times(1)).changeValidationState(any(),any(), eq(ValidationState.DRAFT));
    }
}

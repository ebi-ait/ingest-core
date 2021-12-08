package org.humancellatlas.ingest.biomaterial;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.state.ValidationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class BiomaterialControllerTest {

    @MockBean
    ValidationStateChangeService validationStateChangeService;

    @Autowired
    private MockMvc webApp;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private BiomaterialRepository biomaterialRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @MockBean
    private MessageRouter messageRouter;

    Process process;

    Biomaterial biomaterial;

    @BeforeEach
    void setUp() {
        process = new Process();
        processRepository.save(process);

        biomaterial = new Biomaterial();
        biomaterialRepository.save(biomaterial);
    }

    @Test
    public void testOverrideLinkFileAsInputToMultipleProcessesDefaultPostEndpoint() throws Exception {
        Process process2 = new Process();
        processRepository.save(process2);

        webApp.perform(post("/biomaterials/{id}/inputToProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()
                        + '\n' + ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process2.getId()))
                .andExpect(status().isAccepted());

        verifyInDraft(biomaterial, process, process2);

        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process, process2);
    }

    @Test
    public void testOverrideLinkFileAsInputToMultipleProcessesDefaultPutEndpoint() throws Exception {
        biomaterial.addAsInputToProcess(process);
        biomaterialRepository.save(biomaterial);

        Process process2 = new Process();
        Process process3 = new Process();
        processRepository.save(process2);
        processRepository.save(process3);

        webApp.perform(put("/biomaterials/{id}/inputToProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process2.getId()
                        + '\n' + ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process3.getId()))
                .andExpect(status().isAccepted());

        verifyInDraft(biomaterial, process, process2, process3);

        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process2, process3);
    }


    @Test
    public void testOverrideDefaultInputToProcessesLinkEndpoint() throws Exception {
        webApp.perform(post("/biomaterials/{id}/inputToProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()))
                .andExpect(status().isAccepted());

        verifyInDraft(biomaterial, process);

        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process);
    }

    @Test
    public void testOverrideLinkFileAsDerivedByMultipleProcessesDefaultPostEndpoint() throws Exception {
        Process process2 = new Process();
        processRepository.save(process2);

        webApp.perform(post("/biomaterials/{id}/derivedByProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()
                        + '\n' + ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process2.getId()))
                .andExpect(status().isAccepted());

        verifyInDraft(biomaterial, process, process2);

        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process, process2);
    }

    @Test
    public void testOverrideLinkFileAsDerivedByMultipleProcessesDefaultPutEndpoint() throws Exception {
        biomaterial.addAsDerivedByProcess(process);
        biomaterialRepository.save(biomaterial);

        Process process2 = new Process();
        Process process3 = new Process();
        processRepository.save(process2);
        processRepository.save(process3);

        webApp.perform(put("/biomaterials/{id}/derivedByProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process2.getId()
                        + '\n' + ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process3.getId()))
                .andExpect(status().isAccepted());

        verifyInDraft(biomaterial, process, process2, process3);

        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process2, process3);
    }

    @Test
    public void testOverrideDefaultDerivedByProcessesLinkEndpoint() throws Exception {
        webApp.perform(post("/biomaterials/{id}/derivedByProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/processes/" + process.getId()))
                .andExpect(status().isAccepted());

        verifyInDraft(biomaterial, process);

        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process);
    }

    @Test
    public void testUnlinkBiomaterialAsInputToProcessChangesTheirValidationStatesToDraft() throws Exception {
        // given
        biomaterial.addAsInputToProcess(process);
        biomaterialRepository.save(biomaterial);

        // when
        webApp.perform(delete("/biomaterials/{id}/inputToProcesses/{processId}", biomaterial.getId(), process.getId()))
                .andExpect(status().isNoContent());

        // then
        verifyInDraft(biomaterial, process);

        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getInputToProcesses()).doesNotContain(process);
    }

    @Test
    public void testDeleteDerivedByProcessChangesTheirValidationStatesToDraft() throws Exception {
        // given
        biomaterial.addAsDerivedByProcess(process);
        biomaterialRepository.save(biomaterial);

        // when
        webApp.perform(delete("/biomaterials/{id}/derivedByProcesses/{processId}", biomaterial.getId(), process.getId()))
                .andExpect(status().isNoContent());

        // then
        verifyInDraft(biomaterial, process);

        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getDerivedByProcesses()).doesNotContain(process);
    }

    private void verifyInDraft(MetadataDocument... values) {
        Arrays.stream(values).forEach(value -> {
            verify(validationStateChangeService, times(1)).changeValidationState(value.getType(), value.getId(), ValidationState.DRAFT);
        });
    }
}
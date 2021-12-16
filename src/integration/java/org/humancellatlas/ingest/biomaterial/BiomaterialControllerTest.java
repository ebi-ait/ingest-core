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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    Process process2;

    Process process3;

    Biomaterial biomaterial;

    UriComponentsBuilder uriBuilder;

    @BeforeEach
    void setUp() {
        process = new Process();
        process2 = new Process();
        process3 = new Process();
        processRepository.saveAll(Arrays.asList(process, process2, process3));

        biomaterial = new Biomaterial();
        biomaterialRepository.save(biomaterial);

        uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
    }

    @Test
    public void testLinkBiomaterialAsInputToProcessesUsingPostMethodWithManyProcessesInPayload() throws Exception {
        // when
        webApp.perform(post("/biomaterials/{id}/inputToProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process.getId()
                        + '\n' + uriBuilder.build().toUriString() + "/processes/" + process2.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraft(biomaterial, process, process2);
        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process, process2);
    }

    @Test
    public void testLinkBiomaterialAsInputToProcessesUsingPutMethodWithManyProcessesInPayload() throws Exception {
        // given
        biomaterial.addAsInputToProcess(process);
        biomaterialRepository.save(biomaterial);

        // when
        webApp.perform(put("/biomaterials/{id}/inputToProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process2.getId()
                        + '\n' + uriBuilder.build().toUriString() + "/processes/" + process3.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraft(biomaterial, process, process2, process3);
        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process2, process3);
    }


    @Test
    public void testLinkBiomaterialAsInputToProcessesUsingPostMethodWithOneProcessInPayload() throws Exception {
        //when
        webApp.perform(post("/biomaterials/{id}/inputToProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraft(biomaterial, process);
        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process);
    }

    @Test
    public void testLinkBiomaterialAsDerivedByProcessesUsingPostMethodWithManyProcessesInPayload() throws Exception {
        // when
        webApp.perform(post("/biomaterials/{id}/derivedByProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process.getId()
                        + '\n' + uriBuilder.build().toUriString() + "/processes/" + process2.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraft(biomaterial, process, process2);
        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process, process2);
    }

    @Test
    public void testLinkBiomaterialAsDerivedByProcessesUsingPutMethodWithManyProcessesInPayload() throws Exception {
        // given
        biomaterial.addAsDerivedByProcess(process);
        biomaterialRepository.save(biomaterial);

        // when
        webApp.perform(put("/biomaterials/{id}/derivedByProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process2.getId()
                        + '\n' + uriBuilder.build().toUriString() + "/processes/" + process3.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraft(biomaterial, process, process2, process3);
        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process2, process3);
    }

    @Test
    public void testLinkBiomaterialAsDerivedByProcessesUsingPostMethodWithOneProcessInPayload() throws Exception {
        // when
        webApp.perform(post("/biomaterials/{id}/derivedByProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process.getId()))
                .andExpect(status().isOk());

        verifyThatValidationStateChangedToDraft(biomaterial, process);

        // then
        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process);
    }

    @Test
    public void testUnlinkBiomaterialAsInputToProcesses() throws Exception {
        // given
        biomaterial.addAsInputToProcess(process);
        biomaterialRepository.save(biomaterial);

        // when
        webApp.perform(delete("/biomaterials/{id}/inputToProcesses/{processId}", biomaterial.getId(), process.getId()))
                .andExpect(status().isNoContent());

        // then
        verifyThatValidationStateChangedToDraft(biomaterial, process);

        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getInputToProcesses()).doesNotContain(process);
    }

    @Test
    public void testUnlinkBiomaterialAsDerivedByProcesses() throws Exception {
        // given
        biomaterial.addAsDerivedByProcess(process);
        biomaterialRepository.save(biomaterial);

        // when
        webApp.perform(delete("/biomaterials/{id}/derivedByProcesses/{processId}", biomaterial.getId(), process.getId()))
                .andExpect(status().isNoContent());

        // then
        verifyThatValidationStateChangedToDraft(biomaterial, process);
        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getDerivedByProcesses()).doesNotContain(process);
    }

    private void verifyThatValidationStateChangedToDraft(MetadataDocument... values) {
        Arrays.stream(values).forEach(value -> {
            verify(validationStateChangeService, times(1)).changeValidationState(value.getType(), value.getId(), ValidationState.DRAFT);
        });
    }
}
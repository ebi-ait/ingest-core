package org.humancellatlas.ingest.biomaterial;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.ValidationStateChangeService;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
@AutoConfigureDataMongo()
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
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @MockBean
    private MessageRouter messageRouter;

    Process process1;

    Process process2;

    Process process3;

    Biomaterial biomaterial;

    UriComponentsBuilder uriBuilder;

    SubmissionEnvelope submissionEnvelope;

    Project project;

    @BeforeEach
    void setUp() {
        submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setUuid(Uuid.newUuid());
        submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALID);
        submissionEnvelope = submissionEnvelopeRepository.save(submissionEnvelope);

        project = new Project(null);
        project.getSubmissionEnvelopes().add(submissionEnvelope);
        project = projectRepository.save(project);

        process1 = processRepository.save(new Process(null));
        process2 = processRepository.save(new Process(null));
        process3 = processRepository.save(new Process(null));

        biomaterial = new Biomaterial();
        biomaterial.setSubmissionEnvelope(submissionEnvelope);
        biomaterial = biomaterialRepository.save(biomaterial);

        uriBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
    }

    @AfterEach
    void tearDown() {
        processRepository.deleteAll();
        biomaterialRepository.deleteAll();
        submissionEnvelopeRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    public void newBiomaterialInSubmissionLinksToSubmissionAndProject() throws Exception {
        //given
        biomaterialRepository.deleteAll();
        processRepository.deleteAll();

        // when
        webApp.perform(
            post("/submissionEnvelopes/{id}/biomaterials", submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}")
        ).andExpect(status().isAccepted());

        //then
        assertThat(biomaterialRepository.findAll()).hasSize(1);
        assertThat(biomaterialRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);
        assertThat(biomaterialRepository.findByProject(project)).hasSize(1);

        var newBiomaterial = biomaterialRepository.findAll().get(0);
        assertThat(newBiomaterial.getSubmissionEnvelope().getId()).isEqualTo(submissionEnvelope.getId());
        assertThat(newBiomaterial.getProject().getId()).isEqualTo(project.getId());
        assertThat(newBiomaterial.getProjects()).hasSize(1);
        assertThat(newBiomaterial.getProjects().stream().findFirst().get().getId()).isEqualTo(project.getId());
    }

    @Test
    public void newBiomaterialInSubmissionDoesNotFailIfSubmissionHasNoProject() throws Exception {
        //given
        biomaterialRepository.deleteAll();
        processRepository.deleteAll();
        projectRepository.deleteAll();

        // when
        webApp.perform(
            post("/submissionEnvelopes/{id}/biomaterials", submissionEnvelope.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": {}}")
        ).andExpect(status().isAccepted());

        //then
        assertThat(biomaterialRepository.findAll()).hasSize(1);
        assertThat(biomaterialRepository.findAllBySubmissionEnvelope(submissionEnvelope)).hasSize(1);

        var newBiomaterial = biomaterialRepository.findAll().get(0);
        assertThat(newBiomaterial.getSubmissionEnvelope().getId()).isEqualTo(submissionEnvelope.getId());
        assertThat(newBiomaterial.getProject()).isNull();
        assertThat(newBiomaterial.getProjects()).isEmpty();
    }

    @Test
    public void testLinkBiomaterialAsInputToProcessesUsingPostMethodWithManyProcessesInPayload() throws Exception {
        // when
        webApp.perform(post("/biomaterials/{id}/inputToProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process1.getId()
                        + '\n' + uriBuilder.build().toUriString() + "/processes/" + process2.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(biomaterial);
        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process1, process2);
    }

    @Test
    public void testLinkBiomaterialAsInputToProcessesUsingPutMethodWithManyProcessesInPayload() throws Exception {
        // given
        biomaterial.addAsInputToProcess(process1);
        biomaterialRepository.save(biomaterial);

        // when
        webApp.perform(put("/biomaterials/{id}/inputToProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process2.getId()
                        + '\n' + uriBuilder.build().toUriString() + "/processes/" + process3.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(biomaterial);
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
                .content(uriBuilder.build().toUriString() + "/processes/" + process1.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(biomaterial);
        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getInputToProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process1);
    }

    @Test
    public void testLinkBiomaterialAsDerivedByProcessesUsingPostMethodWithManyProcessesInPayload() throws Exception {
        // when
        webApp.perform(post("/biomaterials/{id}/derivedByProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process1.getId()
                        + '\n' + uriBuilder.build().toUriString() + "/processes/" + process2.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(biomaterial);
        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process1, process2);
    }

    @Test
    public void testLinkBiomaterialAsDerivedByProcessesUsingPutMethodWithManyProcessesInPayload() throws Exception {
        // given
        biomaterial.addAsDerivedByProcess(process1);
        biomaterialRepository.save(biomaterial);

        // when
        webApp.perform(put("/biomaterials/{id}/derivedByProcesses/", biomaterial.getId())
                .contentType("text/uri-list")
                .content(uriBuilder.build().toUriString() + "/processes/" + process2.getId()
                        + '\n' + uriBuilder.build().toUriString() + "/processes/" + process3.getId()))
                .andExpect(status().isOk());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(biomaterial);
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
                .content(uriBuilder.build().toUriString() + "/processes/" + process1.getId()))
                .andExpect(status().isOk());

        verifyThatValidationStateChangedToDraftWhenGraphValid(biomaterial);

        // then
        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getDerivedByProcesses())
                .usingElementComparatorOnFields("id")
                .containsExactly(process1);
    }

    @Test
    public void testUnlinkBiomaterialAsInputToProcesses() throws Exception {
        // given
        biomaterial.addAsInputToProcess(process1);
        biomaterialRepository.save(biomaterial);

        // when
        webApp.perform(delete("/biomaterials/{id}/inputToProcesses/{processId}", biomaterial.getId(), process1.getId()))
                .andExpect(status().isNoContent());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(biomaterial);

        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getInputToProcesses()).doesNotContain(process1);
    }

    @Test
    public void testUnlinkBiomaterialAsDerivedByProcesses() throws Exception {
        // given
        biomaterial.addAsDerivedByProcess(process1);
        biomaterialRepository.save(biomaterial);

        // when
        webApp.perform(delete("/biomaterials/{id}/derivedByProcesses/{processId}", biomaterial.getId(), process1.getId()))
                .andExpect(status().isNoContent());

        // then
        verifyThatValidationStateChangedToDraftWhenGraphValid(biomaterial);
        Biomaterial updatedBiomaterial = biomaterialRepository.findById(biomaterial.getId()).get();
        assertThat(updatedBiomaterial.getDerivedByProcesses()).doesNotContain(process1);
    }

    private void verifyThatValidationStateChangedToDraftWhenGraphValid(MetadataDocument... values) {
        Arrays.stream(values).forEach(
            value -> verify(validationStateChangeService, times(1))
                .changeValidationState(value.getType(), value.getId(), ValidationState.DRAFT)
        );
    }
}
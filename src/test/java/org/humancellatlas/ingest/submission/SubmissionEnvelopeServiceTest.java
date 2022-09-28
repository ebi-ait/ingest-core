package org.humancellatlas.ingest.submission;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.errors.SubmissionErrorRepository;
import org.humancellatlas.ingest.exporter.Exporter;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.patch.PatchRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.project.ProjectService;
import org.humancellatlas.ingest.project.WranglingState;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.state.SubmissionState;
import org.humancellatlas.ingest.state.SubmitAction;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submissionmanifest.SubmissionManifestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SubmissionEnvelopeService.class})
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class SubmissionEnvelopeServiceTest {
    @Autowired
    private SubmissionEnvelopeService service;

    @MockBean
    private MessageRouter messageRouter;

    @MockBean
    private Exporter exporter;

    @MockBean
    private MetadataCrudService metadataCrudService;

    @MockBean
    private MetadataUpdateService metadataUpdateService;

    @MockBean
    private ExecutorService executorService;

    @MockBean
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @MockBean
    private SubmissionEnvelopeCreateHandler submissionEnvelopeCreateHandler;

    @MockBean
    private SubmissionManifestRepository submissionManifestRepository;

    @MockBean
    private BundleManifestRepository bundleManifestRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private ProcessRepository processRepository;

    @MockBean
    private ProtocolRepository protocolRepository;

    @MockBean
    private FileRepository fileRepository;

    @MockBean
    private BiomaterialRepository biomaterialRepository;

    @MockBean
    private PatchRepository patchRepository;

    @MockBean
    private SubmissionErrorRepository submissionErrorRepository;

    @MockBean
    ProjectService projectService;

    @Configuration
    static class TestConfiguration {
    }

    @Test
    public void testContentLastUpdated() {
        // given
        SubmissionEnvelope submission = mock(SubmissionEnvelope.class);
        Project project = mock(Project.class);
        Biomaterial biomaterial = mock(Biomaterial.class);
        Process process = mock(Process.class);
        File file =  mock(File.class);

        PageRequest request = PageRequest.of(0, 1, new Sort(Sort.Direction.DESC, "updateDate"));
        when(projectRepository.findBySubmissionEnvelopesContaining(submission, request))
            .thenReturn(new PageImpl<>(List.of(project), request, 1));
        when(protocolRepository.findBySubmissionEnvelope(submission, request))
            .thenReturn(Page.empty());
        when(biomaterialRepository.findBySubmissionEnvelope(submission, request))
            .thenReturn(new PageImpl<>(List.of(biomaterial), request, 1));
        when(processRepository.findBySubmissionEnvelope(submission, request))
            .thenReturn(new PageImpl<>(List.of(process), request, 1));
        when(fileRepository.findBySubmissionEnvelope(submission, request))
            .thenReturn(new PageImpl<>(List.of(file), request, 1));

        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        when(project.getUpdateDate()).thenReturn(yesterday);
        when(biomaterial.getUpdateDate()).thenReturn(yesterday);
        when(process.getUpdateDate()).thenReturn(yesterday);
        when(file.getUpdateDate()).thenReturn(now);

        // when
        Optional<Instant> lastUpdateDate = service.getSubmissionContentLastUpdated(submission);

        // then
        assertThat(lastUpdateDate.isPresent()).isTrue();
        assertThat(lastUpdateDate.get().toString()).isEqualTo(now.toString());
    }

    @Test
    public void testContentLastUpdatedEmptySubmission() {
        // given
        SubmissionEnvelope submission = mock(SubmissionEnvelope.class);

        PageRequest request = PageRequest.of(0, 1, new Sort(Sort.Direction.DESC, "updateDate"));
        when(projectRepository.findBySubmissionEnvelopesContaining(submission, request))
            .thenReturn(Page.empty());
        when(protocolRepository.findBySubmissionEnvelope(submission, request))
            .thenReturn(Page.empty());
        when(biomaterialRepository.findBySubmissionEnvelope(submission, request))
            .thenReturn(Page.empty());
        when(processRepository.findBySubmissionEnvelope(submission, request))
            .thenReturn(Page.empty());
        when(fileRepository.findBySubmissionEnvelope(submission, request))
            .thenReturn(Page.empty());

        // when
        Optional<Instant> lastUpdateDate = service.getSubmissionContentLastUpdated(submission);

        // then
        assertThat(lastUpdateDate.isPresent()).isFalse();
    }

    @Test
    public void testDeleteSubmission() {
        //given SubmissionEnvelope
        SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setUuid(Uuid.newUuid());

        //given metadata within the SubmissionEnvelope
        Biomaterial testBiomaterial = new Biomaterial(Map.ofEntries(Map.entry("key", UUID.randomUUID())));
        Protocol testProtocol = new Protocol(Map.ofEntries(Map.entry("key", UUID.randomUUID())));
        Process testProcess = new Process(Map.ofEntries(Map.entry("key", UUID.randomUUID())));

        testProcess.setSubmissionEnvelope(submissionEnvelope);
        testProtocol.setSubmissionEnvelope(submissionEnvelope);
        testBiomaterial.setSubmissionEnvelope(submissionEnvelope);

        //given metadata outside the SubmissionEnvelope
        Biomaterial testOutsideBiomaterial = new Biomaterial(Map.ofEntries(Map.entry("key", UUID.randomUUID())));
        File testOutsideFile = new File(Map.ofEntries(Map.entry("key", UUID.randomUUID())), "");
        Process testOutsideProcess = new Process(Map.ofEntries(Map.entry("key", UUID.randomUUID())));

        //given links to metadata outside of the SubmissionEnvelope
        testOutsideBiomaterial.getInputToProcesses().add(testProcess);
        testOutsideFile.getDerivedByProcesses().add(testProcess);
        testOutsideProcess.getProtocols().add(testProtocol);

        //given File
        File file = new File(null, "testFile.txt");
        file.setSubmissionEnvelope(submissionEnvelope);

        //given Project
        Project project = new Project(new Object());
        project.setUuid(Uuid.newUuid());
        project.addToSubmissionEnvelopes(submissionEnvelope);
        assertThat(project.getSubmissionEnvelopes()).contains(submissionEnvelope);

        //given SupplementaryFile
        project.getSupplementaryFiles().add(file);
        assertThat(project.getSupplementaryFiles()).contains(file);

        //given ProjectRepository
        List<Project> projectList = new ArrayList<>();
        projectList.add(project);
        when(projectRepository.findBySubmissionEnvelopesContaining(any(), any()))
                .thenReturn(new PageImpl<>(projectList, Pageable.unpaged(), 1));

        when(projectRepository.findBySubmissionEnvelopesContains(any()))
                .thenReturn(Stream.of(project));

        when(projectRepository.findBySupplementaryFilesContains(any()))
                .thenReturn(Stream.of(project));

        //when
        when(processRepository.findBySubmissionEnvelope(submissionEnvelope)).thenReturn(Stream.of(testProcess));
        when(biomaterialRepository.findBySubmissionEnvelope(submissionEnvelope)).thenReturn(Stream.of(testBiomaterial));
        when(protocolRepository.findBySubmissionEnvelope(submissionEnvelope)).thenReturn(Stream.of(testProtocol));
        when(fileRepository.findBySubmissionEnvelope(submissionEnvelope)).thenReturn(Stream.of(file));

        when(biomaterialRepository.findByInputToProcessesContains(testProcess)).thenReturn(Stream.of(testOutsideBiomaterial));
        when(fileRepository.findByDerivedByProcessesContains(testProcess)).thenReturn(Stream.of(testOutsideFile));
        when(processRepository.findByProtocolsContains(testProtocol)).thenReturn(Stream.of(testOutsideProcess));

        service.deleteSubmission(submissionEnvelope, false);

        //then:
        verify(metadataCrudService).removeLinksToDocument(testProcess);
        verify(metadataCrudService).removeLinksToDocument(testProtocol);
        verify(metadataCrudService).removeLinksToDocument(file);

        verify(biomaterialRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(processRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(protocolRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(fileRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(bundleManifestRepository).deleteByEnvelopeUuid(submissionEnvelope.getUuid().getUuid().toString());
        verify(patchRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(submissionManifestRepository).deleteBySubmissionEnvelope(submissionEnvelope);
        verify(submissionErrorRepository).deleteBySubmissionEnvelope(submissionEnvelope);

        verify(projectRepository).findBySubmissionEnvelopesContains(submissionEnvelope);
        assertThat(project.getSubmissionEnvelopes()).isEmpty();
        verify(projectRepository, atLeastOnce()).save(project);
        verify(submissionEnvelopeRepository).delete(submissionEnvelope);
    }

    @ParameterizedTest
    @EnumSource(value = SubmissionState.class, names = {
        "PENDING",
        "DRAFT",
        "METADATA_VALIDATING",
        "METADATA_VALID",
        "METADATA_INVALID",
        "GRAPH_VALIDATION_REQUESTED",
        "GRAPH_VALIDATING",
        "GRAPH_VALID",
        "GRAPH_INVALID",
        "SUBMITTED",
        "PROCESSING",
        "ARCHIVING",
        "ARCHIVED",
        "EXPORTING",
        "EXPORTED",
        "CLEANUP",
        "COMPLETE"
    })
    public void testRedundantHandleEnvelopeStateUpdateRequest(SubmissionState state) {
        // Given
        var submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.enactStateTransition(state);

        // When
        service.handleEnvelopeStateUpdateRequest(submissionEnvelope, state);

        // Then
        // no errors
    }

    @Nested
    @DisplayName("SubmitRequestTests")
    class SubmitRequestTests {
        SubmissionEnvelope submissionEnvelope;
        Project project;

        @BeforeEach
        public void setup() {
            submissionEnvelope = new SubmissionEnvelope();
            submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALID);
            project = new Project(null);
            project.setValidationState(ValidationState.VALID);
            when(projectRepository.findBySubmissionEnvelopesContains(any()))
                    .thenReturn(Stream.of(project));
        }

        @Test
        public void testSubmissionBlocked() {
            //given:
            submissionEnvelope.enactStateTransition(SubmissionState.METADATA_VALID);

            //when
            Throwable exception = assertThrows(RuntimeException.class,
                    () -> service.handleSubmitRequest(submissionEnvelope, List.of(SubmitAction.EXPORT))
            );

            // then:
            assertErrorMessageContains(exception, "without a graph valid state");
        }

        @Test
        public void testSubmissionUnblocked() {
            //when
            service.handleSubmitRequest(submissionEnvelope, List.of(SubmitAction.EXPORT));

            // then:
            verify(submissionEnvelopeRepository).save(submissionEnvelope);
        }

        @Test
        public void testGraphValidationErrorsCleared() {
            //given envelope:
            submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_INVALID);

            //given metadata within the SubmissionEnvelope
            Biomaterial testBiomaterial = new Biomaterial(Map.ofEntries(Map.entry("key", UUID.randomUUID())));
            Protocol testProtocol = new Protocol(Map.ofEntries(Map.entry("key", UUID.randomUUID())));
            Process testProcess = new Process(Map.ofEntries(Map.entry("key", UUID.randomUUID())));
            File testFile = new File(null, "testFile.txt");

            testProcess.setSubmissionEnvelope(submissionEnvelope);
            testProtocol.setSubmissionEnvelope(submissionEnvelope);
            testBiomaterial.setSubmissionEnvelope(submissionEnvelope);
            testFile.setSubmissionEnvelope(submissionEnvelope);

            // given graph validation errors on the metadata
            testBiomaterial.setGraphValidationErrors(Arrays.asList("test1", "test2"));
            testProcess.setGraphValidationErrors(Arrays.asList("test1", "test2"));
            testProtocol.setGraphValidationErrors(Arrays.asList("test1", "test2"));
            testFile.setGraphValidationErrors(Arrays.asList("test1", "test2"));

            // when
            when(biomaterialRepository.findBySubmissionEnvelope(any()))
                    .thenReturn(Stream.of(testBiomaterial));
            when(processRepository.findBySubmissionEnvelope(any()))
                    .thenReturn(Stream.of(testProcess));
            when(protocolRepository.findBySubmissionEnvelope(any()))
                    .thenReturn(Stream.of(testProtocol));
            when(fileRepository.findBySubmissionEnvelope(any()))
                    .thenReturn(Stream.of(testFile));

            service.handleEnvelopeStateUpdateRequest(submissionEnvelope, SubmissionState.GRAPH_VALIDATION_REQUESTED);
            submissionEnvelope.enactStateTransition(SubmissionState.GRAPH_VALIDATION_REQUESTED);
            //then:
            assertThat(submissionEnvelope.getSubmissionState()).isEqualTo(SubmissionState.GRAPH_VALIDATION_REQUESTED);
            assertThat(testBiomaterial.getGraphValidationErrors()).isEqualTo(new ArrayList<>());
            assertThat(testProcess.getGraphValidationErrors()).isEqualTo(new ArrayList<>());
            assertThat(testProtocol.getGraphValidationErrors()).isEqualTo(new ArrayList<>());
            assertThat(testFile.getGraphValidationErrors()).isEqualTo(new ArrayList<>());
        }

        @Test
        public void testSubmissionInvalidProject() {
            //given:
            project.setValidationState(ValidationState.INVALID);

            //when
            Throwable exception = assertThrows(RuntimeException.class,
                    () -> service.handleSubmitRequest(submissionEnvelope, List.of(SubmitAction.EXPORT))
            );

            // then:
            assertErrorMessageContains(exception, "cannot be submitted when the project is invalid");
        }

        @Test
        public void testSubmissionNoProject() {
            //given:
            when(projectRepository.findBySubmissionEnvelopesContains(any()))
                    .thenReturn(Stream.empty());

            //when
            Throwable exception = assertThrows(RuntimeException.class,
                    () -> service.handleSubmitRequest(submissionEnvelope, List.of(SubmitAction.EXPORT))
            );

            // then:
            assertErrorMessageContains(exception, "cannot be submitted without a project");
        }

        @Test void testExportedEventUpdatesHistory() {
            // given
            // submission from setUp()
            // when
            service.handleCommitExported(submissionEnvelope);

            // then
            verify(projectService).updateWranglingState(project, WranglingState.SUBMITTED);
        }
        private void assertErrorMessageContains(Throwable exception, String s) {
            assertThat(exception.getMessage()).contains(s);
            verify(submissionEnvelopeRepository, never()).save(submissionEnvelope);
        }
    }

    @Nested
    @DisplayName("StateUpdateRequestTests")
    class StateUpdateRequestTests {
        SubmissionEnvelope submissionEnvelope;
        HashSet<SubmitAction> submitActions;

        @BeforeEach
        public void setup() {
            submissionEnvelope = new SubmissionEnvelope();
            submissionEnvelope.enactStateTransition(SubmissionState.SUBMITTED);
            submitActions = new HashSet<>();
            submissionEnvelope.setSubmitActions(submitActions);
        }

        @Test
        public void testHandleEnvelopeArchivingRequest(){
            // given
            submitActions.add(SubmitAction.ARCHIVE);

            // when
            service.handleCommitSubmit(submissionEnvelope);

            // then
            verify(messageRouter).routeStateTrackingUpdateMessageForEnvelopeEvent(submissionEnvelope, SubmissionState.PROCESSING);
        }

        @Test
        public void testHandleEnvelopeExportingDataRequest(){
            // given
            submitActions.add(SubmitAction.EXPORT);

            // when
            service.handleCommitSubmit(submissionEnvelope);

            // then
            verify(messageRouter).routeStateTrackingUpdateMessageForEnvelopeEvent(submissionEnvelope, SubmissionState.EXPORTING);
        }

        @Test
        public void testHandleEnvelopeExportingMetadataRequest(){
            // given
            submitActions.add(SubmitAction.EXPORT_METADATA);

            // when
            service.handleCommitSubmit(submissionEnvelope);

            // then
            verify(messageRouter).routeStateTrackingUpdateMessageForEnvelopeEvent(submissionEnvelope, SubmissionState.EXPORTING);
        }

        @Test
        public void testHandleEnvelopeCleanupRequest(){
            // given
            submissionEnvelope.enactStateTransition(SubmissionState.EXPORTED);
            submitActions.add(SubmitAction.CLEANUP);

            // when
            service.handleCommitSubmit(submissionEnvelope);

            // then
            verify(messageRouter).routeStateTrackingUpdateMessageForEnvelopeEvent(submissionEnvelope, SubmissionState.CLEANUP);
        }
    }
}

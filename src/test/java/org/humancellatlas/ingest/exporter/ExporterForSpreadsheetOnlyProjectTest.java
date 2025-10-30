package org.humancellatlas.ingest.exporter;

import static org.humancellatlas.ingest.export.destination.ExportDestinationName.DCP;

import org.json.simple.JSONObject;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.export.destination.ExportDestination;
import org.humancellatlas.ingest.export.job.web.ExportJobRequest;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.core.Uuid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.export.entity.ExportEntityService;
import org.humancellatlas.ingest.bundle.BundleManifestService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.Set;
import java.util.stream.Stream;

import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.export.job.ExportJobService;
import org.humancellatlas.ingest.export.job.ExportJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {DefaultExporter.class}
    )
public class ExporterForSpreadsheetOnlyProjectTest {
    @Autowired
    private Exporter exporter;

    @MockBean
    private ProcessService processService;

    @MockBean
    private MessageRouter messageRouter;

    @MockBean
    private MetadataCrudService metadataCrudService;

    @MockBean
    private ExportJobService exportJobService;

    @MockBean
    private ExportJobRepository exportJobRepository;

    @MockBean
    private ProcessRepository processRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private ExportEntityService exportEntityService;

    @MockBean
    private BundleManifestService bundleManifestService;

    @MockBean
    private BundleManifestRepository bundleManifestRepository;

    // Adding MigrationConfiguration as a MockBean is needed as otherwise MigrationConfiguration won't be initialised.
    @MockBean
    private MigrationConfiguration migrationConfiguration;

    SubmissionEnvelope submissionEnvelope;

    Project project;

    Set<String> assayIds;

    @BeforeEach
    void setUp() {
        submissionEnvelope = new SubmissionEnvelope();
        submissionEnvelope.setUuid(Uuid.newUuid());

        project = new Project(null);
        project.setUuid(Uuid.newUuid());
        project.getSubmissionEnvelopes().add(submissionEnvelope);


        doReturn(Set.of()).when(processService).findAssays(any(SubmissionEnvelope.class));
        
    }

    @Test
    void testExportManifests() {
        //when:
        ExportJob newExportJob = mockCreateExportJob(projectUuid());

        exporter.exportMetadata(newExportJob);

        //then:
        verify(messageRouter).sendExperimentForExport(any(ExperimentProcess.class), any(ExportJob.class), any());

    }
    private String projectUuid() {
        return project.getUuid().getUuid().toString();
    }
    private ExportJob mockCreateExportJob(String projectUuidUuid) {
        var destinationContext = new JSONObject();
        destinationContext.put("projectUuid", projectUuidUuid);

        var exportJobContext = new JSONObject();
        exportJobContext.put("totalAssayCount", 0);
        exportJobContext.put("dataFileTransfer", false);
        ExportJob newExportJob = ExportJob.builder()
                .submission(submissionEnvelope)
                .destination(new ExportDestination(DCP, "v2", destinationContext))
                .context(exportJobContext)
                .build();
        doReturn(newExportJob).when(exportJobService).createExportJob(any(SubmissionEnvelope.class), any(ExportJobRequest.class));
        doReturn(newExportJob).when(exportJobRepository).insert(any(ExportJob.class));
        doReturn(Stream.of(project)).when(projectRepository).findBySubmissionEnvelopesContains(any(SubmissionEnvelope.class));
        return newExportJob;
    }
}

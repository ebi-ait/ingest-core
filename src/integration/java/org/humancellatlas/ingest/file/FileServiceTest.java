package org.humancellatlas.ingest.file;

import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.Checksums;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.exception.CoreEntityNotFoundException;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.file.web.FileMessage;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.state.MetadataDocumentEventHandler;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.OptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest
public class FileServiceTest {
    @MockBean
    MigrationConfiguration migrationConfiguration;

    @MockBean
    FileRepository fileRepository;

    @MockBean
    SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @MockBean
    BiomaterialRepository biomaterialRepository;

    @MockBean
    ProcessRepository processRepository;

    @MockBean
    MetadataDocumentEventHandler metadataDocumentEventHandler;

    @MockBean
    MetadataCrudService metadataCrudService;

    @MockBean
    MetadataUpdateService metadataUpdateService;

    @Autowired
    FileService fileService;

    @Autowired
    private ApplicationContext applicationContext;

    FileMessage fileMessage;

    SubmissionEnvelope submissionEnvelope;

    File file;

    @BeforeEach
    void setUp() {
        applicationContext.getBeansWithAnnotation(MockBean.class).forEach(Mockito::reset);

        Checksums checksums = new Checksums("sha1", "sha256", "crc32c", "s3Etag");
        String submissionUuid = UUID.randomUUID().toString();
        String filename = "filename";
        fileMessage = new FileMessage("cloudUrl", filename, submissionUuid, "content_type", checksums, 123);

        submissionEnvelope = new SubmissionEnvelope("submission1");

        file = new File(null, filename);
        List<File> files = new ArrayList<>();
        files.add(file);

        when(submissionEnvelopeRepository.findByUuid(any(Uuid.class))).thenReturn(submissionEnvelope);
        when(fileRepository.findBySubmissionEnvelopeAndFileName(submissionEnvelope, fileMessage.getFileName())).thenReturn(files);
        when(fileRepository.save(file)).thenReturn(file);
    }

    @Test
    public void testCreateFileFromFileMessage() throws CoreEntityNotFoundException {
        //given:
        when(fileRepository.findBySubmissionEnvelopeAndFileName(submissionEnvelope, fileMessage.getFileName())).thenReturn(new ArrayList<>());

        //when:
        fileService.createFileFromFileMessage(fileMessage);

        //then:
        verify(metadataCrudService).addToSubmissionEnvelopeAndSave(any(File.class), any(SubmissionEnvelope.class));
    }

    @Test
    public void testCreateFileFromFileMessageNotCreated() throws CoreEntityNotFoundException {
        //when:
        fileService.createFileFromFileMessage(fileMessage);

        //then:
        verify(metadataCrudService, never()).addToSubmissionEnvelopeAndSave(any(File.class), any(SubmissionEnvelope.class));
    }


    @Test
    public void testUpdateFileFromFileMessage() throws CoreEntityNotFoundException {
        //when:
        fileService.updateFileFromFileMessage(fileMessage);

        //then:
        verify(fileRepository).save(file);
        assertThat(file.getCloudUrl()).isEqualTo(fileMessage.getCloudUrl());
        assertThat(file.getChecksums()).isEqualTo(fileMessage.getChecksums());
        assertThat(file.getFileContentType()).isEqualTo(fileMessage.getContentType());
        assertThat(file.getValidationState()).isEqualTo(ValidationState.DRAFT);
    }

    @Test
    public void testUpdateFileFromFileMessageRetry() throws CoreEntityNotFoundException {
        //given:
        when(fileRepository.save(file))
                .thenThrow(new OptimisticLockingFailureException("Error"))
                .thenReturn(file);

        //when:
        fileService.updateFileFromFileMessage(fileMessage);

        //then:
        verify(fileRepository, times(2)).save(file);
    }

}


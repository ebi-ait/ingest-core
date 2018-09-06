package org.humancellatlas.ingest.core.service;

import lombok.*;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialService;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileService;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectService;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolService;
import org.humancellatlas.ingest.state.ValidationState;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ValidationStateChangeService {
    private final @NonNull BiomaterialCrudStrategy biomaterialCrudStrategy;
    private final @NonNull ProcessCrudStrategy processCrudStrategy;
    private final @NonNull ProtocolCrudStrategy protocolCrudStrategy;
    private final @NonNull ProjectCrudStrategy projectCrudStrategy;
    private final @NonNull FileCrudStrategy fileCrudStrategy;

    public MetadataDocument changeValidationState(String metadataType,
                                                              String metadataId,
                                                              ValidationState validationState) {
        MetadataCrudStrategy crudStrategy = crudStrategyForMetadataType(metadataType);
        MetadataDocument metadataDocument = crudStrategy.findMetadataDocument(metadataId);
        metadataDocument.setValidationState(validationState);
        metadataDocument = crudStrategy.saveMetadataDocument(metadataDocument);

        return metadataDocument;
    }

    private MetadataCrudStrategy crudStrategyForMetadataType(String metadataType) {
        switch (metadataType) {
            case "biomaterials":
                return biomaterialCrudStrategy;
            case "processes":
                return processCrudStrategy;
            case "protocols":
                return protocolCrudStrategy;
            case "projects":
                return projectCrudStrategy;
            case "files":
                return fileCrudStrategy;
            default:
                throw new RuntimeException(String.format("No such metadata type: %s", metadataType));
        }
    }

    interface MetadataCrudStrategy <T extends MetadataDocument> {
        T saveMetadataDocument(T document);
        T findMetadataDocument(String id);
    }

    @AllArgsConstructor
    @Component
    @Getter
    class BiomaterialCrudStrategy implements MetadataCrudStrategy<Biomaterial> {
        private final @NonNull BiomaterialService biomaterialService;

        @Override
        public Biomaterial saveMetadataDocument(Biomaterial document) {
            return getBiomaterialService().getBiomaterialRepository().save(document);
        }

        @Override
        public Biomaterial findMetadataDocument(String id) {
            return getBiomaterialService().getBiomaterialRepository().findOne(id);
        }
    }

    @Component
    @AllArgsConstructor
    @Getter
    class ProcessCrudStrategy implements MetadataCrudStrategy<Process> {
        private final @NonNull ProcessService processService;

        @Override
        public Process saveMetadataDocument(Process document) {
            return getProcessService().getProcessRepository().save(document);
        }

        @Override
        public Process findMetadataDocument(String id) {
            return getProcessService().getProcessRepository().findOne(id);
        }
    }

    @Component
    @AllArgsConstructor
    @Getter
    class ProtocolCrudStrategy implements MetadataCrudStrategy<Protocol> {
        private final @NonNull ProtocolService protocolService;

        @Override
        public Protocol saveMetadataDocument(Protocol document) {
            return getProtocolService().getProtocolRepository().save(document);
        }

        @Override
        public Protocol findMetadataDocument(String id) {
            return getProtocolService().getProtocolRepository().findOne(id);
        }
    }

    @Component
    @AllArgsConstructor
    @Getter
    class ProjectCrudStrategy implements MetadataCrudStrategy<Project> {
        private final @NonNull ProjectService projectService;

        @Override
        public Project saveMetadataDocument(Project document) {
            return getProjectService().getProjectRepository().save(document);
        }

        @Override
        public Project findMetadataDocument(String id) {
            return getProjectService().getProjectRepository().findOne(id);
        }
    }

    @Component
    @AllArgsConstructor
    @Getter
    class FileCrudStrategy implements MetadataCrudStrategy<File> {
        private final @NonNull FileService fileService;

        @Override
        public File saveMetadataDocument(File document) {
            return getFileService().getFileRepository().save(document);
        }

        @Override
        public File findMetadataDocument(String id) {
            return getFileService().getFileRepository().findOne(id);
        }
    }
}
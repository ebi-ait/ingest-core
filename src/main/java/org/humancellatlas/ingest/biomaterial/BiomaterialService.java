package org.humancellatlas.ingest.biomaterial;

import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Getter
public class BiomaterialService {
  private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
  private final @NonNull BiomaterialRepository biomaterialRepository;
  private final @NonNull ProcessRepository processRepository;
  private final @NonNull ProjectRepository projectRepository;
  private final @NonNull MetadataUpdateService metadataUpdateService;
  private final @NonNull MetadataCrudService metadataCrudService;
  private final Logger log = LoggerFactory.getLogger(getClass());

  protected Logger getLog() {
    return log;
  }

  public Biomaterial addBiomaterialToSubmissionEnvelope(
      final SubmissionEnvelope submissionEnvelope, final Biomaterial biomaterial) {
    if (!biomaterial.getIsUpdate()) {
      projectRepository
          .findBySubmissionEnvelopesContains(submissionEnvelope)
          .findFirst()
          .ifPresent(
              project -> {
                biomaterial.setProject(project);
                biomaterial.getProjects().add(project);
              });
      return metadataCrudService.addToSubmissionEnvelopeAndSave(biomaterial, submissionEnvelope);
    } else {
      return metadataUpdateService.acceptUpdate(biomaterial, submissionEnvelope);
    }
  }

  public Biomaterial addChildBiomaterial(
      final String parentId, final Biomaterial childBiomaterial) {
    final Biomaterial parentBiomaterial =
        biomaterialRepository
            .findById(parentId)
            .orElseThrow(() -> new RuntimeException("Parent biomaterial not found"));

    parentBiomaterial.addChildBiomaterial(childBiomaterial);
    biomaterialRepository.save(childBiomaterial);

    return biomaterialRepository.save(parentBiomaterial);
  }

  public Biomaterial removeChildBiomaterial(final String parentId, final String childId) {
    final Biomaterial parentBiomaterial =
        biomaterialRepository
            .findById(parentId)
            .orElseThrow(() -> new RuntimeException("Parent biomaterial not found"));

    final Biomaterial childBiomaterial =
        biomaterialRepository
            .findById(childId)
            .orElseThrow(() -> new RuntimeException("Child biomaterial not found"));

    parentBiomaterial.removeChildBiomaterial(childBiomaterial);
    biomaterialRepository.save(childBiomaterial);

    return biomaterialRepository.save(parentBiomaterial);
  }

  public Biomaterial addParentBiomaterial(
      final String childId, final Biomaterial parentBiomaterial) {
    final Biomaterial childBiomaterial =
        biomaterialRepository
            .findById(childId)
            .orElseThrow(() -> new RuntimeException("Child biomaterial not found"));

    childBiomaterial.addParentBiomaterial(parentBiomaterial);
    biomaterialRepository.save(parentBiomaterial);

    return biomaterialRepository.save(childBiomaterial);
  }

  public Biomaterial removeParentBiomaterial(final String childId, final String parentId) {
    final Biomaterial childBiomaterial =
        biomaterialRepository
            .findById(childId)
            .orElseThrow(() -> new RuntimeException("Child biomaterial not found"));

    final Biomaterial parentBiomaterial =
        biomaterialRepository
            .findById(parentId)
            .orElseThrow(() -> new RuntimeException("Parent biomaterial not found"));

    childBiomaterial.removeParentBiomaterial(parentBiomaterial);
    biomaterialRepository.save(parentBiomaterial);

    return biomaterialRepository.save(childBiomaterial);
  }
}

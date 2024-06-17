package org.humancellatlas.ingest.core.web;

import java.util.Optional;

import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.patch.Patch;
import org.humancellatlas.ingest.state.ValidationState;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MetadataDocumentResourceProcessor
    implements ResourceProcessor<Resource<MetadataDocument>> {

  private final @NonNull EntityLinks entityLinks;

  @NonNull private final RepositoryEntityLinks repositoryEntityLinks;

  private Optional<Link> getStateTransitionLink(
      MetadataDocument metadataDocument, ValidationState targetState) {
    Optional<String> transitionResourceName = getSubresourceNameForValidationState(targetState);
    if (transitionResourceName.isPresent()) {
      Optional<String> rel = getRelNameForValidationState(targetState);
      if (rel.isPresent()) {
        return Optional.of(
            entityLinks
                .linkForSingleResource(metadataDocument)
                .slash(transitionResourceName.get())
                .withRel(rel.get()));
      } else {
        String messageTemplate =
            "Unexpected link/rel mismatch exception " + "(link = '%s', rel = '%s')";
        throw new RuntimeException(
            String.format(messageTemplate, transitionResourceName.toString(), rel.toString()));
      }
    } else {
      return Optional.empty();
    }
  }

  private Optional<String> getRelNameForValidationState(ValidationState validationState) {
    switch (validationState) {
      case DRAFT:
        return Optional.of(Links.DRAFT_REL);
      case VALIDATING:
        return Optional.of(Links.METADATA_VALIDATING_REL);
      case VALID:
        return Optional.of(Links.METADATA_VALID_REL);
      case INVALID:
        return Optional.of(Links.INVALID_REL);
      case PROCESSING:
        return Optional.of(Links.PROCESSING_REL);
      case COMPLETE:
        return Optional.of(Links.COMPLETE_REL);
      default:
        // default returns no links (not expecting external user interaction)
        return Optional.empty();
    }
  }

  private Optional<String> getSubresourceNameForValidationState(ValidationState validationState) {
    switch (validationState) {
      case DRAFT:
        return Optional.of(Links.DRAFT_URL);
      case VALIDATING:
        return Optional.of(Links.METADATA_VALIDATING_URL);
      case VALID:
        return Optional.of(Links.METADATA_VALID_URL);
      case INVALID:
        return Optional.of(Links.INVALID_URL);
      case PROCESSING:
        return Optional.of(Links.PROCESSING_URL);
      case COMPLETE:
        return Optional.of(Links.COMPLETE_URL);
      default:
        // default returns no links (not expecting external user interaction)
        return Optional.empty();
    }
  }

  @Override
  public Resource<MetadataDocument> process(Resource<MetadataDocument> resource) {
    MetadataDocument metadataDocument = resource.getContent();
    addStateLinks(resource, metadataDocument);
    if (metadataDocument.getIsUpdate()) {
      addPatchLink(resource, metadataDocument.getId());
    }
    return resource;
  }

  private void addStateLinks(
      Resource<MetadataDocument> resource, MetadataDocument metadataDocument) {
    metadataDocument.allowedStateTransitions().stream()
        .map(validationState -> getStateTransitionLink(metadataDocument, validationState))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(resource::add);
  }

  private void addPatchLink(Resource<MetadataDocument> resource, String documentId) {
    Link link =
        repositoryEntityLinks
            .linkToSearchResource(Patch.class, "WithUpdateDocument")
            .withRel("patch")
            .expand(documentId);
    resource.add(link);
  }
}

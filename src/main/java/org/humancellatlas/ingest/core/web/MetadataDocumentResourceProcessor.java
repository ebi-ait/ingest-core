package org.humancellatlas.ingest.core.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.state.ValidationState;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 12/09/17
 */
@Component
@RequiredArgsConstructor
public class MetadataDocumentResourceProcessor implements ResourceProcessor<Resource<MetadataDocument>> {
    private final @NonNull EntityLinks entityLinks;

    private Optional<Link> getStateTransitionLink(MetadataDocument metadataDocument, ValidationState targetState) {
        Optional<String> transitionResourceName = getSubresourceNameForValidationState(targetState);
        if (transitionResourceName.isPresent()) {
            Optional<String> rel = getRelNameForValidationState(targetState);
            if (rel.isPresent()) {
                return Optional.of(entityLinks.linkForSingleResource(metadataDocument)
                                           .slash(transitionResourceName.get())
                                           .withRel(rel.get()));
            }
            else {
                throw new RuntimeException(String.format("Unexpected link/rel mismatch exception (link = '%s', rel = " +
                                                                 "'%s')",
                                                         transitionResourceName.toString(),
                                                         rel.toString()));
            }
        }
        else {
            return Optional.empty();
        }
    }

    private Optional<String> getRelNameForValidationState(ValidationState validationState) {
        switch (validationState) {
            case VALIDATING:
                return Optional.of(Links.VALIDATING_REL);
            case VALID:
                return Optional.of(Links.VALID_REL);
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
            case VALIDATING:
                return Optional.of(Links.VALIDATING_URL);
            case VALID:
                return Optional.of(Links.VALID_URL);
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

    @Override public Resource<MetadataDocument> process(Resource<MetadataDocument> resource) {
        MetadataDocument metadataDocument = resource.getContent();

        metadataDocument.allowedStateTransitions().stream()
                .map(validationState -> getStateTransitionLink(metadataDocument, validationState))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(resource::add);

        return resource;
    }
}

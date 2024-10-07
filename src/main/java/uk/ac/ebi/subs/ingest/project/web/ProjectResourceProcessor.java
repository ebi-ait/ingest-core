package uk.ac.ebi.subs.ingest.project.web;

import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.web.Links;
import uk.ac.ebi.subs.ingest.project.Project;

@Component
@RequiredArgsConstructor
public class ProjectResourceProcessor implements ResourceProcessor<Resource<Project>> {
  private final @NonNull EntityLinks entityLinks;

  @Override
  public Resource<Project> process(Resource<Project> resource) {
    Project project = resource.getContent();
    resource.add(getBundleManifestsLink(project));
    resource.add(getAuditLogsLink(project));

    return resource;
  }

  private Link getBundleManifestsLink(Project project) {
    return entityLinks
        .linkForSingleResource(project)
        .slash(Links.BUNDLE_MANIFESTS_URL)
        .withRel(Links.BUNDLE_MANIFESTS_REL);
  }

  private Link getAuditLogsLink(Project project) {
    return entityLinks
        .linkForSingleResource(project)
        .slash(Links.AUDIT_LOGS_URL)
        .withRel(Links.AUDIT_LOGS_REL);
  }
}

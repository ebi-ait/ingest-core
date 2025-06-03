package uk.ac.ebi.subs.ingest.study.web;

import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.study.Study;

@Component
@RequiredArgsConstructor
public class StudyResourceProcessor implements ResourceProcessor<Resource<Study>> {

  private final @NonNull EntityLinks entityLinks;

  @Override
  public Resource<Study> process(Resource<Study> resource) {
    Study study = resource.getContent();

    if (study != null && study.getId() != null) {
      resource.add(rawDatasetsLink(study));
      resource.add(processedDatasetsLink(study));
      resource.add(analysisDatasetsLink(study));
    }

    return resource;
  }

  private Link rawDatasetsLink(Study study) {
    return new Link(
            "/datasets/search/byStudyAndType?id=" + study.getId() + "&type=raw", "rawDatasets")
        .withTitle("Raw datasets for this study");
  }

  private Link processedDatasetsLink(Study study) {
    return new Link(
            "/datasets/search/byStudyAndType?id=" + study.getId() + "&type=processed",
            "processedDatasets")
        .withTitle("Processed datasets for this study");
  }

  private Link analysisDatasetsLink(Study study) {
    return new Link(
            "/datasets/search/byStudyAndType?id=" + study.getId() + "&type=analysis",
            "analysisDatasets")
        .withTitle("Analysis datasets for this study");
  }
}

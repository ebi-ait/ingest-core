package uk.ac.ebi.subs.ingest.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import uk.ac.ebi.subs.ingest.study.web.StudyResourceProcessor;

class StudyResourceProcessorTest {

  private EntityLinks entityLinks;
  private StudyResourceProcessor studyResourceProcessor;

  @BeforeEach
  void setUp() {
    entityLinks = mock(EntityLinks.class);
    studyResourceProcessor = new StudyResourceProcessor(entityLinks);
  }

  @Test
  void shouldAddDatasetLinksToStudyResource() {
    // Given
    Study study =
        new Study(
            "https://dev.schema.morphic.bio/type/0.0.1/project/study",
            "0.0.1",
            "study",
            Map.of("label", "study B"));
    ReflectionTestUtils.setField(study, "id", "study123");

    // When
    Resource<Study> resource = new Resource<>(study);
    Resource<Study> processed = studyResourceProcessor.process(resource);

    // Then
    assertThat(processed.getLinks()).anyMatch(link -> link.getRel().equals("rawDatasets"));
    assertThat(processed.getLinks()).anyMatch(link -> link.getRel().equals("processedDatasets"));
    assertThat(processed.getLinks()).anyMatch(link -> link.getRel().equals("analysisDatasets"));

    Link rawLink = processed.getLink("rawDatasets");
    assertThat(rawLink).isNotNull();
    assertThat(rawLink.getHref()).contains("type=raw");
    assertThat(rawLink.getTitle()).containsIgnoringCase("Raw");
  }
}

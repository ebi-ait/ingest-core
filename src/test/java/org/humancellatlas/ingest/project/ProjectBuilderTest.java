package org.humancellatlas.ingest.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class ProjectBuilderTest {

  @Test
  public void testProjectBuilder() {
    Project fromBuilder = Project.builder().emptyProject().withManagedAccess().build();

    Project fromCtor = new Project(new HashMap<>());
    ((Map<String, Object>) fromCtor.getContent())
        .put(
            "dataAccess",
            new ObjectToMapConverter().asMap(new DataAccess(DataAccessTypes.MANAGED)));

    assertThat(fromBuilder.getContentLastModified())
        .isCloseTo(fromCtor.getContentLastModified(), within(1, ChronoUnit.SECONDS));
    Comparator<Instant> upToMillies = Comparator.comparing(d -> d.truncatedTo(ChronoUnit.SECONDS));
    assertThat(fromBuilder)
        .usingComparatorForFields(upToMillies, "contentLastModified")
        .isEqualToIgnoringGivenFields(fromCtor, "uuid");
  }
}

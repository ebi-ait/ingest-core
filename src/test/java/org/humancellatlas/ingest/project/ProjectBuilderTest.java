package org.humancellatlas.ingest.project;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class ProjectBuilderTest {

    @Test
    public void testProjectBuilder() {
        Project fromBuilder = Project.builder()
                .emptyProject()
                .withManagedAccess()
                .build();

        Project fromCtor = new Project(new HashMap<>());
        fromCtor.setDataAccess(DataAccessTypes.MANAGED);
        assertThat(fromBuilder.getContentLastModified())
                .isCloseTo(fromCtor.getContentLastModified(), within(1, ChronoUnit.SECONDS));
        Comparator<Instant> upToMillies = Comparator.comparing(d -> d.truncatedTo(ChronoUnit.SECONDS));
        assertThat(fromBuilder)
                .usingComparatorForFields(upToMillies, "contentLastModified")
                .isEqualToIgnoringGivenFields(fromCtor, "uuid")
        ;
    }
}

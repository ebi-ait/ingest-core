package org.humancellatlas.ingest.project;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ProjectBuilderTest {

    @Test
    public void testProjectBuilder() {
        Project fromBuilder = Project.builder()
                .emptyProject()
                .withManagedAccess()
                .build();

        Project fromCtor = new Project(new HashMap<>());
        fromCtor.setDataAccess(DataAccessTypes.MANAGED);
        assertThat(fromBuilder)
                .isEqualToIgnoringGivenFields(fromCtor, "uuid");
    }
}

package org.humancellatlas.ingest.security;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RowLevelFilterSecuritySpelExpressionTest {
    @Test
    public void testSpelContainsWithAPrefix() {
        ExampleDocument document = new ExampleDocument();
        document.listA = Set.of("ROLE_b", "ROLE_c");
        String spelExpression = "#x.listA.contains('ROLE_'+#y.toString())";
        assertThat(new SpelHelper()
                .parseExpression(List.of("x", "y"), List.of(document, "c"),
                        spelExpression)).isTrue();
    }

    class ExampleDocument {
        public Set<String> listA;
    }
}

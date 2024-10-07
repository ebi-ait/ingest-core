package uk.ac.ebi.subs.ingest.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class RowLevelFilterSecuritySpelExpressionTest {
  @Test
  public void testSpelContainsWithAPrefix() {
    ExampleDocument document = new ExampleDocument();
    document.listA = Set.of("ROLE_b", "ROLE_c");
    String spelExpression = "#x.listA.contains('ROLE_'+#y.toString())";
    assertThat(
            new SpelHelper()
                .parseExpression(List.of("x", "y"), List.of(document, "c"), spelExpression))
        .isTrue();
  }

  class ExampleDocument {
    public Set<String> listA;
  }
}

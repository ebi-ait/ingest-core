package uk.ac.ebi.subs.ingest.state;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import uk.ac.ebi.subs.ingest.file.ValidationReport;

@JsonTest
public class ValidationStateTest {
  @Autowired private JacksonTester<ValidationReport> json;

  private static Stream<Arguments> provideStatesForTestFromJSON() {
    return Stream.of(
        Arguments.of(ValidationState.INVALID, "invalid"),
        Arguments.of(ValidationState.INVALID, "Invalid"),
        Arguments.of(ValidationState.INVALID, "INVALID"),
        Arguments.of(ValidationState.VALID, "valid"),
        Arguments.of(ValidationState.VALID, "Valid"),
        Arguments.of(ValidationState.VALID, "VALID"),
        Arguments.of(ValidationState.VALIDATING, "validating"),
        Arguments.of(ValidationState.VALIDATING, "Validating"),
        Arguments.of(ValidationState.VALIDATING, "VALIDATING"));
  }

  @ParameterizedTest
  @MethodSource("provideStatesForTestFromJSON")
  public void testFromJSON(ValidationState expected, String given) throws Exception {
    var jsonValue = String.format("{ \"validationState\": \"%s\" }", given);
    assertThat(json.parse(jsonValue).getObject().getValidationState()).isEqualTo(expected);
  }
}

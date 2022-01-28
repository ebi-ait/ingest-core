package org.humancellatlas.ingest.state;

import org.humancellatlas.ingest.file.ValidationReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@JsonTest
public class ValidationStateTest {
    @Autowired
    private JacksonTester<ValidationReport> json;

    @Test
    public void testFromString() throws Exception{
        var jsonValue = "{ \"validationState\": \"Invalid\" }";
        assertThat(json.parse(jsonValue).getObject().getValidationState()).isEqualTo(ValidationState.INVALID);

        jsonValue = "{ \"validationState\": \"INVALID\" }";
        assertThat(json.parse(jsonValue).getObject().getValidationState()).isEqualTo(ValidationState.INVALID);

        jsonValue = "{ \"validationState\": \"Valid\" }";
        assertThat(json.parse(jsonValue).getObject().getValidationState()).isEqualTo(ValidationState.VALID);

        jsonValue = "{ \"validationState\": \"valid\" }";
        assertThat(json.parse(jsonValue).getObject().getValidationState()).isEqualTo(ValidationState.VALID);
    }
}

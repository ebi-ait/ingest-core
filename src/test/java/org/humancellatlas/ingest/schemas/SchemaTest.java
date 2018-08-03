package org.humancellatlas.ingest.schemas;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaTest {

    @Test
    public void testCompareSame() {
        //given:
        Schema schema = new Schema("core", "7.3.1", "process", "", "process_core",
                "http://schema.humancellatlas.org");

        //expect:
        assertThat(schema.compareTo(schema)).isEqualTo(0);
    }

}

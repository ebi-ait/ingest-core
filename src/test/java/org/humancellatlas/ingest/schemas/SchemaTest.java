package org.humancellatlas.ingest.schemas;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaTest {

    @Test
    public void testCompareSame() {
        //given:
        Schema schema = createTestSchema("7.3.1");

        //expect:
        assertThat(schema.compareTo(schema)).isEqualTo(0);
    }

    @Test
    public void testCompareOlder() {
        //given:
        Schema schemaVersion10 = createTestSchema("10.9");
        Schema schemaVersion7 = createTestSchema("7.3.1");
        Schema schemaVersion6_7 = createTestSchema("6.7.3");
        Schema schemaVersion6_3 = createTestSchema("6.3.11");

        //expect:
        assertThat(schemaVersion10.compareTo(schemaVersion7)).isGreaterThan(0);
        assertThat(schemaVersion7.compareTo(schemaVersion6_7)).isGreaterThan(0);
        assertThat(schemaVersion6_7.compareTo(schemaVersion6_3)).isGreaterThan(0);
    }

    private Schema createTestSchema(String schemaVersion) {
        return new Schema("core", schemaVersion, "process", "", "process_core",
                "http://schema.humancellatlas.org");
    }

}

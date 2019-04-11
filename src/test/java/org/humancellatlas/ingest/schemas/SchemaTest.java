package org.humancellatlas.ingest.schemas;


import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

public class SchemaTest {

    @Test
    public void testCompareToSameVersion() {
        //given:
        Schema schema = createTestSchema("7.3.1");

        //expect:
        assertThat(schema.compareTo(schema)).isEqualTo(0);
    }

    @Test
    public void testCompareToOlderVersion() {
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

    @Test
    public void testCompareToNewerVersion() {
        //given:
        Schema schemaVersion5 = createTestSchema("5");
        Schema schemaVersion5_1 = createTestSchema("5.1");
        Schema schemaVersion5_1_3 = createTestSchema("5.1.3");

        //expect:
        assertThat(schemaVersion5.compareTo(schemaVersion5_1)).isLessThan(0);
        assertThat(schemaVersion5_1.compareTo(schemaVersion5_1_3)).isLessThan(0);
        assertThat(schemaVersion5.compareTo(schemaVersion5_1_3)).isLessThan(0);
    }

    @Test
    public void testCompareDifferentSchemas() {
        //given:
        Schema biomaterialCore = new Schema("core", "5.9.10", "biomaterial", "",
                "biomaterial_core", "http://schema.humancellatlas.org");
        Schema processCore = createTestSchema("7.4.3");
        assumeThat(biomaterialCore.getConcreteEntity())
                .isNotEqualToIgnoringCase(processCore.getConcreteEntity());

        //expect:
        assertThat(biomaterialCore.compareTo(processCore)).isLessThan(0);
        assertThat(processCore.compareTo(biomaterialCore)).isGreaterThan(0);
    }

    private Schema createTestSchema(String schemaVersion) {
        return new Schema("core", schemaVersion, "process", "", "process_core",
                "http://schema.humancellatlas.org");
    }

}

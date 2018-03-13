package org.humancellatlas.ingest.process;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.file.File;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessTest {

    @Test
    public void testIsAssaying() {
        //given:
        Process nonAssayingProcess = new Process();

        //and:
        Process hasInputBiomaterial = new Process();
        hasInputBiomaterial.addInputBiomaterial(new Biomaterial(null));

        //and:
        Process hasDerivedFile = new Process();
        hasDerivedFile.addDerivedFile(new File(null));

        //and:
        Process assayingProcess = new Process();
        assayingProcess.addInputBiomaterial(new Biomaterial(null));
        assayingProcess.addDerivedFile(new File(null));

        //expect:
        String notAssaying = "Expected process to be NON assaying.";
        assertThat(nonAssayingProcess.isAssaying()).as(notAssaying).isFalse();
        assertThat(hasInputBiomaterial.isAssaying()).as(notAssaying).isFalse();
        assertThat(hasDerivedFile.isAssaying()).as(notAssaying).isFalse();

        //and:
        assertThat(assayingProcess.isAssaying()).as("Expected process to be assaying.").isTrue();
    }

}

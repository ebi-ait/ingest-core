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
        hasInputBiomaterial.addInput(new Biomaterial(null));

        //and:
        Process hasDerivedFile = new Process();
        hasDerivedFile.addDerivative(new File(null));

        //and:
        Process assayingProcess = new Process();
        assayingProcess.addInput(new Biomaterial(null));
        assayingProcess.addDerivative(new File(null));

        //expect:
        String notAssaying = "Expected process to be NON assaying.";
        assertThat(nonAssayingProcess.isAssaying()).as(notAssaying).isFalse();
        assertThat(hasInputBiomaterial.isAssaying()).as(notAssaying).isFalse();
        assertThat(hasDerivedFile.isAssaying()).as(notAssaying).isFalse();

        //and:
        assertThat(assayingProcess.isAssaying()).as("Expected process to be assaying.").isTrue();
    }

    @Test
    public void testIsAnalysis() {
        //given:
        Process nonAnalysis = new Process();

        //then:
        assertThat(nonAnalysis.isAnalysis()).isFalse();
    }

}

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
        String notAssaying = "Expected Process to be NON assaying.";
        assertThat(nonAssayingProcess.isAssaying()).as(notAssaying).isFalse();
        assertThat(hasInputBiomaterial.isAssaying()).as(notAssaying).isFalse();
        assertThat(hasDerivedFile.isAssaying()).as(notAssaying).isFalse();

        //and:
        assertThat(assayingProcess.isAssaying()).as("Expected Process to be assaying.").isTrue();
    }

    @Test
    public void testIsAnalysis() {
        //given:
        Process nonAnalysis = new Process();

        //and:
        Process hasInputFile = new Process();
        hasInputFile.addInput(new File("input"));

        //and:
        Process hasDerivedFile = new Process();
        hasDerivedFile.addDerivative(new File("output"));

        //and:
        Process analysis = new Process();
        analysis.addInput(new File("input"));
        analysis.addDerivative(new File("output"));

        //then:
        String notAnalysis = "Expected Process to be non Analysis";
        assertThat(nonAnalysis.isAnalysis()).as(notAnalysis).isFalse();
        assertThat(hasInputFile.isAnalysis()).as(notAnalysis).isFalse();
        assertThat(hasDerivedFile.isAnalysis()).as(notAnalysis).isFalse();

        //then:
        assertThat(analysis.isAnalysis()).as("Expected Process to be Analysis.").isTrue();
    }

}

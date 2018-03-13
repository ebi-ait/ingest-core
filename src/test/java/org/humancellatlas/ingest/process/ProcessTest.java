package org.humancellatlas.ingest.process;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessTest {

    @Test
    public void testIsAssaying() {
        //given:
        Process notAssaying = new Process();

        //expect:
        assertThat(notAssaying.isAssaying());
    }

}

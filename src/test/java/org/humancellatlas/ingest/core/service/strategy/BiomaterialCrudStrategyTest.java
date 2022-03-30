package org.humancellatlas.ingest.core.service.strategy;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.service.strategy.impl.BiomaterialCrudStrategy;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {BiomaterialCrudStrategy.class})
public class BiomaterialCrudStrategyTest {
    @Autowired private BiomaterialCrudStrategy biomaterialCrudStrategy;

    @MockBean private BiomaterialRepository biomaterialRepository;
    @MockBean private MessageRouter messageRouter;

    private Biomaterial testBiomaterial;

    @BeforeEach
    void setUp() {
        // ToDo: Extract the test constructors into test classes
        testBiomaterial = new Biomaterial("biomaterialId");
    }

    @Test
    public void testDeleteBiomaterial() {
        //when
        biomaterialCrudStrategy.deleteDocument(testBiomaterial);
        //then
        verify(biomaterialRepository).delete(testBiomaterial);
    }
}

package uk.ac.ebi.subs.ingest.core.service.strategy;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.ac.ebi.subs.ingest.biomaterial.Biomaterial;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.core.service.strategy.impl.BiomaterialCrudStrategy;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {BiomaterialCrudStrategy.class})
public class BiomaterialCrudStrategyTest {
  @Autowired private BiomaterialCrudStrategy biomaterialCrudStrategy;

  @MockBean private BiomaterialRepository biomaterialRepository;
  @MockBean private MessageRouter messageRouter;

  private Biomaterial testBiomaterial;

  @BeforeEach
  void setUp() {
    testBiomaterial = new Biomaterial(null);
  }

  @Test
  public void testDeleteBiomaterial() {
    // when
    biomaterialCrudStrategy.deleteDocument(testBiomaterial);
    // then
    verify(biomaterialRepository).delete(testBiomaterial);
  }
}

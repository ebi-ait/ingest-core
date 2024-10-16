package uk.ac.ebi.subs.ingest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import uk.ac.ebi.subs.ingest.config.MigrationConfiguration;

@SpringBootTest
public class IngestCoreApplicationTests {
  @MockBean MigrationConfiguration migrationConfiguration;

  @Test
  public void contextLoads() {}
}

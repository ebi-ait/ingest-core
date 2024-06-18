package org.humancellatlas.ingest;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class IngestCoreApplicationTests {
  @MockBean MigrationConfiguration migrationConfiguration;

  @Test
  public void contextLoads() {}
}

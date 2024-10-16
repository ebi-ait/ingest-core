package uk.ac.ebi.subs.ingest.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.config.MigrationConfiguration;
import uk.ac.ebi.subs.ingest.file.FileRepository;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.protocol.ProtocolRepository;

@SpringBootTest
@AutoConfigureDataMongo()
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class ManagedAccessConfigurationTest {
  @Autowired FileRepository fileRepository;

  @Autowired BiomaterialRepository biomaterialRepository;

  @Autowired ProtocolRepository protocolRepository;

  @Autowired ProcessRepository processRepository;
  // NOTE: Adding MigrationConfiguration as a MockBean is needed
  // as otherwise MigrationConfiguration won't be initialised.
  @MockBean private MigrationConfiguration migrationConfiguration;

  @Test
  public void testDBRepositoriesHaveAnnotation() {
    Stream.builder()
        .add(fileRepository)
        .add(biomaterialRepository)
        .add(protocolRepository)
        .add(processRepository)
        .build()
        .forEach(
            r ->
                assertThat(
                    Arrays.stream(r.getClass().getAnnotations())
                        .anyMatch(
                            annotation ->
                                annotation.annotationType().equals(RowLevelFilterSecurity.class))));
  }
}

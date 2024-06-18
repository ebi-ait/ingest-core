package org.humancellatlas.ingest.core.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.protocol.Protocol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SpringLinkGenerator.class})
public class SpringLinkGeneratorTest {

  @MockBean private ResourceMappings mappings;

  @Autowired private SpringLinkGenerator linkGenerator = new SpringLinkGenerator();

  @Test
  public void testCreateCallback() {
    ResourceMetadata resourceMetadata = mock(ResourceMetadata.class);
    when(resourceMetadata.getRel()).thenReturn("metadata");
    when(mappings.getMetadataFor(any())).thenReturn(resourceMetadata);

    // when:
    String processCallback = linkGenerator.createCallback(Process.class, "df00e2");
    String biomaterialCallback = linkGenerator.createCallback(Biomaterial.class, "c80122");
    String fileCallback = linkGenerator.createCallback(File.class, "98dd90");
    String protocolCallback = linkGenerator.createCallback(Protocol.class, "846df1");
    String bmCallback = linkGenerator.createCallback(BundleManifest.class, "332fd9");

    // then:
    assertThat(processCallback).isEqualToIgnoringCase("/metadata/df00e2");
    assertThat(biomaterialCallback).isEqualToIgnoringCase("/metadata/c80122");
    assertThat(fileCallback).isEqualToIgnoringCase("/metadata/98dd90");
    assertThat(protocolCallback).isEqualToIgnoringCase("/metadata/846df1");
    assertThat(bmCallback).isEqualToIgnoringCase("/metadata/332fd9");
  }
}

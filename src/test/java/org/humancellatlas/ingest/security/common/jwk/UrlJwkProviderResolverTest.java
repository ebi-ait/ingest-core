package org.humancellatlas.ingest.security.common.jwk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.auth0.jwk.UrlJwkProvider;

public class UrlJwkProviderResolverTest {

  @Test
  public void testResolve() {
    // given:
    String baseUrl = "https://sample.service.tld";
    UrlJwkProviderResolver resolver = new UrlJwkProviderResolver(baseUrl);

    // when:
    String relativePath = "issuer.service.tld";
    UrlJwkProvider provider = resolver.resolve(relativePath);

    // then:
    assertThat(provider).isNotNull();

    // and: inspect assigned URL through sub-class interface as a work-around
    var url = ((RemoteJwkProvider) provider).getUrl();
    assertThat(url.toString()).isEqualTo("https://sample.service.tld/issuer.service.tld");
  }
}

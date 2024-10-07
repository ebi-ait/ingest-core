package uk.ac.ebi.subs.ingest.security.common.jwk;

import java.net.MalformedURLException;
import java.net.URL;

import com.auth0.jwk.UrlJwkProvider;

public class UrlJwkProviderResolver {

  private final URL baseUrl;

  public UrlJwkProviderResolver(String baseUrl) {
    try {
      this.baseUrl = new URL(baseUrl);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  // TODO cache providers based on relative path
  public UrlJwkProvider resolve(String relativePath) {
    try {
      var providerUrl = new URL(baseUrl, relativePath);
      return new RemoteJwkProvider(providerUrl);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public UrlJwkProvider resolve() {
    return new RemoteJwkProvider(this.baseUrl);
  }
}

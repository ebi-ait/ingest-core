package org.humancellatlas.ingest.security.common.jwk;

import java.net.URL;

import com.auth0.jwk.UrlJwkProvider;

/** A UrlJwkProvider that allows inspection of its internal URL. */
public class RemoteJwkProvider extends UrlJwkProvider {

  private final URL url;

  public RemoteJwkProvider(URL url) {
    super(url);
    this.url = url;
  }

  public URL getUrl() {
    return url;
  }
}

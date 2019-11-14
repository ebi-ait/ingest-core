package org.humancellatlas.ingest.security.jwk;

import com.auth0.jwk.UrlJwkProvider;

import java.net.URL;

/**
 * A UrlJwkProvider that allows inspection of its internal URL.
 */
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

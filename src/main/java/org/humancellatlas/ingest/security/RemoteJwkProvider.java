package org.humancellatlas.ingest.security;

import com.auth0.jwk.UrlJwkProvider;

import java.net.URL;

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

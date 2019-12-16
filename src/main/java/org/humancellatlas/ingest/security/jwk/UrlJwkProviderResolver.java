package org.humancellatlas.ingest.security.jwk;

import com.auth0.jwk.UrlJwkProvider;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlJwkProviderResolver {

    private final URL baseUrl;

    public UrlJwkProviderResolver(String baseUrl) {
        try {
            this.baseUrl = new URL(baseUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO cache providers based on relative path
    public UrlJwkProvider resolve(String relativePath) {
        var providerUrl = resolveUrl(relativePath);
        return new RemoteJwkProvider(providerUrl);
    }

    public URL resolveUrl(String relativePath) {
        try {
            return new URL(baseUrl, relativePath);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}

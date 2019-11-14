package org.humancellatlas.ingest.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

public class JwtGenerator {

    public static final String DEFAULT_ISSUER = "https://humancellatlas.auth0.com";
    public static final String DEFAULT_KEY_ID = "MDc2OTM3ODI4ODY2NUU5REVGRDVEM0MyOEYwQTkzNDZDRDlEQzNBRQ";

    private final KeyPair keyPair;

    private final String issuer;

    public JwtGenerator() {
        this(DEFAULT_ISSUER);
    }

    public JwtGenerator(@Nonnull String issuer) {
        this.issuer = issuer;
        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(2048);
            this.keyPair = keyGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public RSAPublicKey getPublicKey() {
        return (RSAPublicKey) keyPair.getPublic();
    }

    public String generate() {
        return generate(null, null);
    }

    public String generate(Map<String, String> claims) {
        return generate(null, claims);
    }

    public String generate(@Nullable String keyId, @Nullable Map<String, String> claims) {
        var kid = Optional.ofNullable(keyId);
        Map<String, Object> header = Map.ofEntries(entry("kid", kid.orElse(DEFAULT_KEY_ID)));

        JWTCreator.Builder builder = JWT.create().withHeader(header).withIssuer(issuer);
        Optional.ofNullable(claims)
                .ifPresent(existentClaims -> existentClaims.forEach(builder::withClaim));

        var rsa256 = Algorithm.RSA256(null, (RSAPrivateKey) keyPair.getPrivate());
        return builder.sign(rsa256);
    }

}

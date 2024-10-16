package uk.ac.ebi.subs.ingest.security;

import static java.util.Map.entry;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

import javax.annotation.Nullable;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ebi.subs.ingest.security.authn.oidc.UserInfo;

public class JwtGenerator {

  public static final String DEFAULT_ISSUER = "https://humancellatlas.auth0.com";
  public static final String DEFAULT_KEY_ID =
      "MDc2OTM3ODI4ODY2NUU5REVGRDVEM0MyOEYwQTkzNDZDRDlEQzNBRQ";

  public static final String OIDC_ISS = "iss";
  public static final String OIDC_SUB = "sub";

  private final ObjectMapper objectMapper = new ObjectMapper();

  private final KeyPair keyPair;

  private final String issuer;

  public JwtGenerator() {
    this(DEFAULT_ISSUER);
  }

  /**
   * Creates an instance with a pre-defined default issuer. This issuer <b>>will be overridden</b>
   * by "iss" claim during when generating the JWT if it is set.
   *
   * @param issuer
   */
  // The decision to allow "iss" claim to override the issuer field is so that UserInfo can be
  // encoded with little
  // modifications to this utility class.
  public JwtGenerator(@Nullable String issuer) {
    KeyPairGenerator keyGenerator = getKeyPairGenerator();
    this.keyPair = keyGenerator.generateKeyPair();
    this.issuer = Optional.ofNullable(issuer).orElse(DEFAULT_ISSUER);
  }

  private KeyPairGenerator getKeyPairGenerator() {
    try {
      KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
      keyGenerator.initialize(2048);
      return keyGenerator;
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public RSAPublicKey getPublicKey() {
    return (RSAPublicKey) keyPair.getPublic();
  }

  public String generate() {
    return generate(null, null, null);
  }

  public String generate(Map<String, String> claims) {
    return generate(null, null, claims);
  }

  public String generate(
      @Nullable String keyId, @Nullable String subject, @Nullable Map<String, String> claims) {
    var kid = Optional.ofNullable(keyId);
    Map<String, Object> header = Map.ofEntries(entry("kid", kid.orElse(DEFAULT_KEY_ID)));

    Map<String, String> allClaims = new HashMap<>();
    Optional.ofNullable(claims).ifPresent(allClaims::putAll);
    JWTCreator.Builder builder =
        JWT.create()
            .withHeader(header)
            .withIssuer(Optional.ofNullable(allClaims.get(OIDC_ISS)).orElse(issuer))
            .withSubject(
                Optional.ofNullable(subject)
                    .or(() -> Optional.ofNullable(allClaims.get(OIDC_SUB)))
                    .orElse(UUID.randomUUID().toString()));

    Arrays.asList(OIDC_ISS, OIDC_SUB).forEach(allClaims::remove);
    allClaims.forEach(builder::withClaim);

    var rsa256 = Algorithm.RSA256(null, (RSAPrivateKey) keyPair.getPrivate());
    return builder.sign(rsa256);
  }

  public String generateWithSubject(String subject) {
    return generate(null, subject, null);
  }

  public String encode(UserInfo userInfo) {
    var claims = objectMapper.convertValue(userInfo, new TypeReference<Map<String, String>>() {});
    return generate(null, null, claims);
  }
}

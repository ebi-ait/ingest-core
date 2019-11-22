package org.humancellatlas.ingest.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.spring.security.api.authentication.JwtAuthentication;

public class User {
    private DecodedJWT token;

    public User(JwtAuthentication jwtAuthentication) {
        this.token = JWT.decode(jwtAuthentication.getToken());
    }

    public String getGroup() {
        String claimName = "https://auth.data.humancellatlas.org/group";
        return token.getClaim(claimName).asString();
    }
}

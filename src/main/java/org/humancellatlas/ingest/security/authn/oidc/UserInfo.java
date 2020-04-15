package org.humancellatlas.ingest.security.authn.oidc;

import com.auth0.jwt.interfaces.DecodedJWT;

public class UserInfo {

    private final String subjectId;
    private final String issuer;

    public UserInfo(String subjectId, String issuer) {
        this.subjectId = subjectId;
        this.issuer = issuer;
    }

    public UserInfo(DecodedJWT decodedJWT) {
        this.subjectId = decodedJWT.getSubject();
        this.issuer = decodedJWT.getIssuer();
    }

    public String getSubjectId() {
        return subjectId;
    }

    public boolean hasIssuer() {
        return issuer != null && !issuer.isEmpty();
    }

}

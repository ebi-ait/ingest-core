package org.humancellatlas.ingest.security.authn.oidc;

public class UserInfo {

    private final String subjectId;
    private final String issuer;

    public UserInfo(String subjectId, String issuer) {
        this.subjectId = subjectId;
        this.issuer = issuer;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public boolean hasIssuer() {
        return issuer != null && !issuer.isEmpty();
    }

}

package org.humancellatlas.ingest.security.authn.oidc;

public class UserInfo {

    private final String subjectId;

    public UserInfo(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectId() {
        return subjectId;
    }

}

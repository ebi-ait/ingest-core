package org.humancellatlas.ingest.security.authn.oidc;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.humancellatlas.ingest.security.Account;

public class UserInfo {

    private final String subjectId;
    private final String issuer;
    private final String name;

    public UserInfo(String subjectId, String issuer, String name) {
        this.subjectId = subjectId;
        this.issuer = issuer;
        this.name = name;
    }

    //TODO remove this
    public UserInfo(DecodedJWT decodedJWT) {
        this.subjectId = decodedJWT.getSubject();
        this.issuer = decodedJWT.getIssuer();
        this.name = "";
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getName() {
        return name;
    }

    public boolean hasIssuer() {
        return issuer != null && !issuer.isEmpty();
    }

    public Account toAccount() {
        Account account = new Account(subjectId);
        account.setName(name);
        return account;
    }

}

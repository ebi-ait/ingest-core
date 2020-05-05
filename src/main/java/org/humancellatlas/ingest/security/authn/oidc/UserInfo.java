package org.humancellatlas.ingest.security.authn.oidc;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.humancellatlas.ingest.security.Account;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserInfo {

    @JsonProperty("sub")
    private String subjectId;

    private String name;

    @JsonProperty("preferred_username")
    private String preferredUsername;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    private String email;
    private String issuer;

    public UserInfo(String subjectId, String issuer, String name) {
        this.subjectId = subjectId;
        this.issuer = issuer;
        this.name = name;
    }

    //TODO remove this
    //UserInfo should be independent of Auth0's classes
    public UserInfo(DecodedJWT decodedJWT) {
        this.subjectId = decodedJWT.getSubject();
        this.issuer = decodedJWT.getIssuer();
        this.name = "";
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

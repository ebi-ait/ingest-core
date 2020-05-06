package org.humancellatlas.ingest.security.authn.oidc;

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

    @JsonProperty("iss")
    private String issuer;

    public UserInfo(String subjectId, String issuer, String name) {
        this.subjectId = subjectId;
        this.issuer = issuer;
        this.name = name;
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

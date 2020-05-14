package org.humancellatlas.ingest.security.authn.oidc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.humancellatlas.ingest.security.Account;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter(AccessLevel.PROTECTED)
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

    public UserInfo(String subjectId, String name) {
        this.subjectId = subjectId;
        this.name = name;
    }

    public Account toAccount() {
        Account account = new Account(subjectId);
        account.setName(name);
        return account;
    }

}

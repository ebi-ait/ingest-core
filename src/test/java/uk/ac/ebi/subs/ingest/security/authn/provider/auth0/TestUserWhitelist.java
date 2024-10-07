package uk.ac.ebi.subs.ingest.security.authn.provider.auth0;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import uk.ac.ebi.subs.ingest.security.authn.provider.gcp.GcpDomainWhiteList;

public class TestUserWhitelist {

  @Test
  public void testLists() {
    // given:
    GcpDomainWhiteList userWhiteList =
        new GcpDomainWhiteList("trusteddomain.com", "friendlypeople.net");

    // expect:
    asList(
            "goodguy@trusteddomain.com",
            "upstandinglass@friendlypeople.net",
            "cooldude@friendlypeople.net")
        .forEach(email -> assertThat(userWhiteList.lists(email)).isTrue());

    // and:
    asList("maninavan@shadycharacters.tv", "suspicious@darkcorner.xyz")
        .forEach(email -> assertThat(userWhiteList.lists(email)).isFalse());
  }
}

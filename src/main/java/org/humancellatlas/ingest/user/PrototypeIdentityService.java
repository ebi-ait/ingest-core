package org.humancellatlas.ingest.user;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrototypeIdentityService implements IdentityService {
  private final Environment environment;

  @Override
  public String wranglerEmail() {
    return environment.getProperty("WRANGLER_EMAILS",
                                   "hca-notifications@ebi.ac.uk");

  }
}

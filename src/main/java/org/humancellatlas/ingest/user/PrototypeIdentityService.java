package org.humancellatlas.ingest.user;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PrototypeIdentityService implements IdentityService {
  private final Environment environment;

  @Override
  public String wranglerEmail() {
    return environment.getProperty("WRANGLER_EMAILS", "hca-notifications-test@ebi.ac.uk");
  }
}

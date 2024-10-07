package uk.ac.ebi.subs.ingest.notifications.processors.impl.email;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SMTPConfig {
  private final String host;
  private final int port;
  private final String username;
  private final String password;
}

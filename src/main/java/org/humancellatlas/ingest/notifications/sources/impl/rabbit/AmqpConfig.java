package org.humancellatlas.ingest.notifications.sources.impl.rabbit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AmqpConfig {
  private final String sendExchange;
  private final String sendRoutingKey;
}

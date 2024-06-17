package org.humancellatlas.ingest.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Message {
  private String exchange;
  private String routingKey;
  private Object payload;
}

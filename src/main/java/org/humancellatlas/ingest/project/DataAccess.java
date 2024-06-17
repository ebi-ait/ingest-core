package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.*;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode
public class DataAccess {
  @JsonSerialize(using = DataAccessTypesJsonSerializer.class)
  @JsonDeserialize(using = DataAccessTypesJsonDeserializer.class)
  private final @NonNull DataAccessTypes type;

  String notes;
}

package org.humancellatlas.ingest.archiving.submission;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.humancellatlas.ingest.archiving.Error;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.Identifiable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Document
@RequiredArgsConstructor
public class ArchiveSubmission implements Identifiable<String> {

  @Id @JsonIgnore private String id;

  @CreatedDate private Instant created;

  @Setter private String dspUuid;

  @Setter private URI dspUrl;

  @Setter private String submissionUuid;

  @Setter private Object fileUploadPlan;

  private @Setter List<Error> errors = new ArrayList<>();
}

package org.humancellatlas.ingest.errors;

import java.net.URI;

import org.zalando.problem.Problem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties({"parameters", "status"})
public class IngestError implements Problem {
  private URI type;
  private String title;
  private String detail;
  private URI instance;

  IngestError(Problem problem) {
    this.type = problem.getType();
    this.title = problem.getTitle();
    this.detail = problem.getDetail();
  }
}

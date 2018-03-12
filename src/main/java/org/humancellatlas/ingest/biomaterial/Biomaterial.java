package org.humancellatlas.ingest.biomaterial;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.project.Project;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rolando on 16/02/2018.
 */
@CrossOrigin
@Getter
public class Biomaterial extends MetadataDocument {
  @RestResource @DBRef private final List<Process> provenantProcesses = new ArrayList<>();
  @RestResource @DBRef private final List<Project> projects = new ArrayList<>();

  @JsonCreator
  public Biomaterial(Object content) {
    super(EntityType.BIOMATERIAL, content);
  }
}

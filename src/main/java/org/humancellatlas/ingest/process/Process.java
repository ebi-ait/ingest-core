package org.humancellatlas.ingest.process;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.protocol.Protocol;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 * Created by rolando on 16/02/2018.
 */
@Getter
public class Process extends MetadataDocument {

  @RestResource @DBRef private final List<Biomaterial> inputBiomaterials = new ArrayList<>();
  @RestResource @DBRef private final List<File> inputFiles = new ArrayList<>();

  @DBRef private final Process inputProcess = null;
  @DBRef private final Protocol protocol = null;


  @JsonCreator
  public Process(Object content) {
    super(EntityType.PROCESS, content);
  }

  public Process() {}
}
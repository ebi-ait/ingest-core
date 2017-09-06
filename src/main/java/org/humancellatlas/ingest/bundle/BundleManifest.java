package org.humancellatlas.ingest.bundle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.file.File;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.hateoas.Identifiable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by rolando on 05/09/2017.
 */
@AllArgsConstructor
@Getter
public class BundleManifest implements Identifiable<String> {
    private @Id @JsonIgnore String id;

    private final Uuid bundleUuid;

    private final List<Uuid> fileUuids;
    private final Map<Uuid, Collection<Uuid>> fileSampleMap;
    private final Map<Uuid, Collection<Uuid>> fileAssayMap;
    private final Map<Uuid, Collection<Uuid>> fileAnalysisMap;
    private final Map<Uuid, Collection<Uuid>> fileProjectMap;
    private final Map<Uuid, Collection<Uuid>> fileProtocolMap;
}

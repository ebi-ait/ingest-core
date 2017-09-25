package org.humancellatlas.ingest.bundle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.humancellatlas.ingest.core.*;
import org.springframework.data.annotation.Id;
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

    private final String bundleUuid;
    private final String envelopeUuid;

    private final List<String> files;
    private final Map<String, Collection<String>> fileSampleMap;
    private final Map<String, Collection<String>> fileAssayMap;
    private final Map<String, Collection<String>> fileAnalysisMap;
    private final Map<String, Collection<String>> fileProjectMap;
    private final Map<String, Collection<String>> fileProtocolMap;
}

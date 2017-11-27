package org.humancellatlas.ingest.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class MetadataReference {
    private final @NonNull
    List<String> uuids;
}

package org.humancellatlas.ingest.schemas;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Created by rolando on 19/04/2018.
 */
@Service
@RequiredArgsConstructor
@Getter
public class SchemaService {
    private final @NonNull SchemaRepository schemaRepository;
}

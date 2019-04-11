package org.humancellatlas.ingest.process;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett

 */
@RequiredArgsConstructor
@Getter
public class BundleReference {
    private final @NonNull List<String> bundleUuids;
}

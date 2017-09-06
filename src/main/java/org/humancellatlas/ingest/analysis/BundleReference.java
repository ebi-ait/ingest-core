package org.humancellatlas.ingest.analysis;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/09/17
 */
@RequiredArgsConstructor
@Getter
public class BundleReference {
    private final @NonNull List<String> bundleUuids;
}

package org.humancellatlas.ingest.process;

import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
public class BundleReference {
    private List<String> bundleUuids;
}

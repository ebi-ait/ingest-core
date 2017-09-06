package org.humancellatlas.ingest.core.web;

/**
 * Enumerates the relations that are available in this application to provide some stability across different
 * implementations. These should not change without serious motivation, as will likely cause backwards-compatibility
 * breaking changes for clients.
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
public class Links {
    // Links for submissions
    public static final String SUBMIT_URL = "/confirmation";
    public static final String SUBMIT_REL = "submit";

    // Links for analyses
    public static final String BUNDLE_REF_URL = "/bundleReferences";
    public static final String BUNDLE_REF_REL = "add-input-bundles";
}

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

    public static final String ANALYSES_URL = "/analyses";
    public static final String ANALYSES_REL = "analyses";
    public static final String ASSAYS_URL = "/assays";
    public static final String ASSAYS_REL = "assays";
    public static final String FILES_URL = "/files";
    public static final String FILES_REL = "files";
    public static final String PROJECTS_URL = "/projects";
    public static final String PROJECTS_REL = "projects";
    public static final String PROTOCOLS_URL = "/protocols";
    public static final String PROTOCOLS_REL = "protocols";
    public static final String SAMPLES_URL = "/samples";
    public static final String SAMPLES_REL = "samples";

    // Links for analyses
    public static final String BUNDLE_REF_URL = "/bundleReferences";
    public static final String BUNDLE_REF_REL = "add-input-bundles";
    public static final String FILE_REF_URL = "/fileReference";
    public static final String FILE_REF_REL = "add-file-reference";
}

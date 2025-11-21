package uk.ac.ebi.subs.ingest.dataset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for file and directory listings returned
 * by the /datasets/{datasetId}/globus/files API endpoint.
 *
 * This structure is consumed by the Python client to display file sizes.
 */
@Data // Generates getters, setters, toString(), equals(), and hashCode()
@Builder // Optional: Allows for fluent object creation (FileListingEntry.builder()...)
@NoArgsConstructor // Required for Jackson deserialization (if used)
@AllArgsConstructor // Generates a constructor with all fields
public class FileListingEntry {

    /**
     * The name of the file or directory, including a trailing '/' for directories.
     */
    private String name;

    /**
     * The type of the entry, typically "file" or "dir".
     */
    private String type;

    /**
     * The size of the file in bytes. Should be null for directories.
     */
    private Long size;
}

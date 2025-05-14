package uk.ac.ebi.subs.ingest.gene;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StudySummary {
    private String id;
    private String studyTitle;
    private String institute;
    private String label;
    private String readoutAssay;
    private List<String> perturbationType;
    private List<String> modelOrganSystems;
    private String draccDataSharingDate;
    private List<String> accessions;
}


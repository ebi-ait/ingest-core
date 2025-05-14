package uk.ac.ebi.subs.ingest.gene;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GeneStudiesResponse {
    private String hgncId;
    private String symbol;
    private List<StudySummary> studies;
}

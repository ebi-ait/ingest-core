package uk.ac.ebi.subs.ingest.study.api;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Value;
import uk.ac.ebi.subs.ingest.study.Study;

import java.util.List;

/** complete payload returned by GET /studies/{id} */
@Value
public class StudyDto {

    /** keep every field of {@link Study} at the top JSON level */
    @JsonUnwrapped
    Study study;

    /** replace the old String[] with the enriched objects */
    @JsonProperty("new_target_genes")        // keep the original key
    List<GeneRef> targetGenes;
}

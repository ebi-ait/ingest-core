package uk.ac.ebi.subs.ingest.submission.web;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

public class MongoAggregationUtils {
  public static Aggregation aggregationPipelineFromStrings(List<String> jsonStages) {
    return Aggregation.newAggregation(
        jsonStages.stream()
            .map(Document::parse)
            .map((Document d) -> (AggregationOperation) context -> context.getMappedObject(d))
            .collect(Collectors.toList()));
  }
}

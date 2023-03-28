package org.humancellatlas.ingest.submission.web;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators.ToString;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.ArrayElemAt.arrayOf;
import static org.springframework.data.mongodb.core.aggregation.Fields.UNDERSCORE_ID;
import static org.springframework.data.mongodb.core.aggregation.ObjectOperators.ObjectToArray.valueOfToArray;


class ProcessAndInputBiomaterials {
    String processId;
    List<String> inputBiomaterials;
}

@Repository
public class SubmissionLinkMapRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    Map<String, SubmissionLinkMapController.ProcessLinkingMap> findProcessLinkings(SubmissionEnvelope submissionEnvelope) {
        Aggregation agg = newAggregation(
                project("inputToProcesses", UNDERSCORE_ID)
                        .and(arrayOf(valueOfToArray("submissionEnvelope"))
                                .elementAt(1))
                        .as("submission_id"),
                project("inputToProcesses", UNDERSCORE_ID)
                        .and(ToString.toString("$submission_id.v"))
                        .as("submission_id"),
                match(Criteria.where("submission_id")
                        .is(submissionEnvelope.getId())),
                unwind("inputToProcesses"),
                project(UNDERSCORE_ID)
                        .and(arrayOf(valueOfToArray("inputToProcesses"))
                                .elementAt(1))
                        .as("process_id"),
                project("process_id")
                        .and(UNDERSCORE_ID).as("biomaterial_id"),
                group("$process_id.v")
                        .addToSet(ToString.toString("$biomaterial_id"))
                        .as("inputBiomaterials"),
                project("inputBiomaterials")
                        .and(ToString.toString("$_id"))
                        .as("processId"),
                sort(Sort.Direction.DESC, "processId")
        );
        AggregationResults<ProcessAndInputBiomaterials> processAndInputBiomaterials = mongoTemplate.aggregate(agg,
                "biomaterial",
                ProcessAndInputBiomaterials.class
        );
        return null;
    }
}

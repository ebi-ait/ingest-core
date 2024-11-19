package uk.ac.ebi.subs.ingest.submission.web;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.ArrayElemAt.arrayOf;
import static org.springframework.data.mongodb.core.aggregation.Fields.UNDERSCORE_ID;
import static org.springframework.data.mongodb.core.aggregation.ObjectOperators.ObjectToArray.valueOfToArray;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators.ToString;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

class ProcessAndInputBiomaterials {
  String processId;
  List<String> inputBiomaterials;
}

class ProcessAndInputFiles {
  String processId;
  List<String> inputFiles;
}

class EntityWithInputsAndDerivedBy {
  String entityId;
  List<String> inputToProcesses;
  List<String> derivedByProcesses;
}

class EntityWithProtocols {
  String entityId;
  List<String> protocols;
}

@Repository
public class SubmissionLinkMapRepository {

  @Autowired MongoTemplate mongoTemplate;

  List<ProcessAndInputBiomaterials> findProcessInputBiomaterials(
      SubmissionEnvelope submissionEnvelope) {
    String entity_type = "biomaterial";
    Aggregation agg =
        buildAggregationQueryForProcessInputs(submissionEnvelope, entity_type, "inputBiomaterials");
    AggregationResults<ProcessAndInputBiomaterials> processAndInputBiomaterials =
        mongoTemplate.aggregate(agg, entity_type, ProcessAndInputBiomaterials.class);
    return processAndInputBiomaterials.getMappedResults();
  }

  private static Aggregation buildAggregationQueryForProcessInputs(
      SubmissionEnvelope submissionEnvelope, String entity_type, String inputBiomaterials) {
    return newAggregation(
        project("inputToProcesses", UNDERSCORE_ID)
            .and(arrayOf(valueOfToArray("submissionEnvelope")).elementAt(1))
            .as("submission_id"),
        project("inputToProcesses", UNDERSCORE_ID)
            .and(ToString.toString("$submission_id.v"))
            .as("submission_id"),
        match(Criteria.where("submission_id").is(submissionEnvelope.getId())),
        unwind("inputToProcesses"),
        project(UNDERSCORE_ID)
            .and(arrayOf(valueOfToArray("inputToProcesses")).elementAt(1))
            .as("process_id"),
        project("process_id").and(UNDERSCORE_ID).as(entity_type + "_id"),
        group("$process_id.v")
            .addToSet(ToString.toString("$" + entity_type + "_id"))
            .as(inputBiomaterials),
        project(inputBiomaterials).and(ToString.toString("$_id")).as("processId"),
        sort(Sort.Direction.DESC, "processId"));
  }

  List<ProcessAndInputFiles> findProcessInputFiles(SubmissionEnvelope submissionEnvelope) {
    String entity_type = "file";
    Aggregation agg =
        buildAggregationQueryForProcessInputs(submissionEnvelope, entity_type, "inputFiles");
    AggregationResults<ProcessAndInputFiles> processAndInputFiles =
        mongoTemplate.aggregate(agg, entity_type, ProcessAndInputFiles.class);
    return processAndInputFiles.getMappedResults();
  }

  List<EntityWithInputsAndDerivedBy> findLinkedProcessesByEntityTypeAndSubmission(
      SubmissionEnvelope submissionEnvelope, String entity_type) {
    List<String> jsonStages =
        List.of(
            "    {\n"
                + "        $project: {\n"
                + "            submission_id: { $arrayElemAt: [{ $objectToArray: \"$submissionEnvelope\" }, 1] },\n"
                + "            inputToProcesses: 1,\n"
                + "            \"derivedByProcesses\": 1,\n"
                + "        }\n"
                + "    }",
            "    {\n"
                + "        $project: {\n"
                + "            submission_id: { $toString: '$submission_id.v' },\n"
                + "            inputToProcesses: 1,\n"
                + "            \"derivedByProcesses\": 1,\n"
                + "        }\n"
                + "    }",
            String.format(
                "{ \"$match\": { \"submission_id\": \"%s\", } }", submissionEnvelope.getId()),
            "{\n"
                + "        $project: {\n"
                + "            \"inputToProcesses\": {\n"
                + "                $map: {\n"
                + "                    input: \"$inputToProcesses\",\n"
                + "                    as: \"process_id\",\n"
                + "                    in: { $arrayElemAt: [{ $objectToArray: \"$$process_id\" }, 1] }\n"
                + "                }\n"
                + "            },\n"
                + "            \"derivedByProcesses\": {\n"
                + "                $map: {\n"
                + "                    input: \"$derivedByProcesses\",\n"
                + "                    as: \"process_id\",\n"
                + "                    in: { $arrayElemAt: [{ $objectToArray: \"$$process_id\" }, 1] }\n"
                + "                }\n"
                + "            },\n"
                + "        }\n"
                + "    }",
            "{\n"
                + "        $project: {\n"
                + "            _id: 0"
                + "            entityId: {$toString:\"$_id\"},"
                + "            \"inputToProcesses\": {\n"
                + "                $map: {\n"
                + "                    input: \"$inputToProcesses\",\n"
                + "                    as: \"process_id\",\n"
                + "                    in: {$toString: \"$$process_id.v\"}\n"
                + "                }\n"
                + "            },\n"
                + "            \"derivedByProcesses\": {\n"
                + "                $map: {\n"
                + "                    input: \"$derivedByProcesses\",\n"
                + "                    as: \"process_id\",\n"
                + "                    in: {$toString: \"$$process_id.v\"}\n"
                + "                }\n"
                + "            },\n"
                + "        }\n"
                + "    }");
    Aggregation aggregation = MongoAggregationUtils.aggregationPipelineFromStrings(jsonStages);
    return mongoTemplate
        .aggregate(aggregation, entity_type, EntityWithInputsAndDerivedBy.class)
        .getMappedResults();
  }

  public List<EntityWithProtocols> findProcessProtocols(SubmissionEnvelope submissionEnvelope) {
    List<String> jsonStages =
        List.of(
            "{\n"
                + "        $project: {\n"
                + "            submission_id: { $arrayElemAt: [{ $objectToArray: \"$submissionEnvelope\" }, 1] },\n"
                + "            protocols: 1,\n"
                + "        }\n"
                + "    },\n",
            "    {\n"
                + "        $project: {\n"
                + "            submission_id: { $toString: '$submission_id.v' },\n"
                + "            protocols: 1,\n"
                + "        }\n"
                + "    },\n",
            String.format(
                "    { \"$match\": { \"submission_id\": \"%s\", } },\n",
                submissionEnvelope.getId()),
            "     {\n"
                + "        $project: {\n"
                + "            \"protocols\": {\n"
                + "                $map: {\n"
                + "                    input: \"$protocols\",\n"
                + "                    as: \"protocol_id\",\n"
                + "                    in: { $arrayElemAt: [{ $objectToArray: \"$$protocol_id\" }, 1] }\n"
                + "                }\n"
                + "            },\n"
                + "        }\n"
                + "    },\n",
            "    {\n"
                + "        $project: {\n"
                + "            _id:0,\n"
                + "            entityId: {$toString:\"$_id\"},\n"
                + "            \"protocols\": {\n"
                + "                $map: {\n"
                + "                    input: \"$protocols\",\n"
                + "                    as: \"protocol_id\",\n"
                + "                    in: {$toString: \"$$protocol_id.v\"}\n"
                + "                }\n"
                + "            },\n"
                + "        }\n"
                + "    }\n");
    Aggregation aggregation = MongoAggregationUtils.aggregationPipelineFromStrings(jsonStages);
    return mongoTemplate
        .aggregate(aggregation, "process", EntityWithProtocols.class)
        .getMappedResults();
  }
}

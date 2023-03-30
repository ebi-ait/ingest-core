package org.humancellatlas.ingest.migrations;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;

@ChangeLog
public class MongoChangeLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoChangeLog.class);

    private static final Integer MONGO_INDEX_NOT_FOUND = 27;

    @ChangeSet(order = "2019-10-30", id = "featureCompatibilityVersion 3.4", author = "alexie.staffer@ebi.ac.uk")
    public void featureCompatibilityThreeFour(MongoDatabase db) {
        if (MongoVersionHelper.featureCompatibilityLessThan(db, "3.4"))
            db.runCommand(new Document("setFeatureCompatibilityVersion", "3.4"));
    }

    @ChangeSet(order = "2019-10-31", id = "featureCompatibilityVersion 3.6", author = "alexie.staffer@ebi.ac.uk")
    public void featureCompatibilityThreeSix(MongoDatabase db) {
        if (MongoVersionHelper.featureCompatibilityLessThan(db, "3.6"))
            db.runCommand(new Document("setFeatureCompatibilityVersion", "3.6"));
    }

    @ChangeSet(order = "2019-11-01", id = "featureCompatibilityVersion 4.0", author = "alexie.staffer@ebi.ac.uk")
    public void featureCompatibilityFourZero(MongoDatabase db) {
        if (MongoVersionHelper.featureCompatibilityLessThan(db, "4.0")) {
            db.runCommand(new Document("setFeatureCompatibilityVersion", "4.0"));
            db.runCommand(new Document("setFreeMonitoring", 1).append("action", "disable"));
        }
    }

    @ChangeSet(order = "2019-11-02", id = "featureCompatibilityVersion 4.2", author = "alexie.staffer@ebi.ac.uk")
    public void featureCompatibilityFourTwo(MongoDatabase db) {
        if (MongoVersionHelper.featureCompatibilityLessThan(db, "4.2"))
            db.runCommand(new Document("setFeatureCompatibilityVersion", "4.2"));
    }

    @ChangeSet(order = "2019-11-03", id = "singletonSubmissionEnvelope Biomaterial", author = "alexie.staffer@ebi.ac.uk")
    public void singletonSubmissionEnvelopeBiomaterial(MongoDatabase db) {
        Document filter = Document.parse("{submissionEnvelopes: {$exists: 1}}");
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ submissionEnvelope: { $arrayElemAt: [ \"$submissionEnvelopes\", 0 ] } }")));
        update.add(new Document("$unset", "submissionEnvelopes"));

        db.getCollection("biomaterial").updateMany(filter, update);
    }

    @ChangeSet(order = "2019-11-04", id = "singletonSubmissionEnvelope Process", author = "alexie.staffer@ebi.ac.uk")
    public void singletonSubmissionEnvelopeProcess(MongoDatabase db) {
        Document filter = Document.parse("{submissionEnvelopes: {$exists: 1}}");
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ submissionEnvelope: { $arrayElemAt: [ \"$submissionEnvelopes\", 0 ] } }")));
        update.add(new Document("$unset", "submissionEnvelopes"));

        db.getCollection("process").updateMany(filter, update);
    }

    @ChangeSet(order = "2019-11-05", id = "singletonSubmissionEnvelope Protocol", author = "alexie.staffer@ebi.ac.uk")
    public void singletonSubmissionEnvelopeProtocol(MongoDatabase db) {
        Document filter = Document.parse("{submissionEnvelopes: {$exists: 1}}");
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ submissionEnvelope: { $arrayElemAt: [ \"$submissionEnvelopes\", 0 ] } }")));
        update.add(new Document("$unset", "submissionEnvelopes"));

        db.getCollection("protocol").updateMany(filter, update);
    }

    @ChangeSet(order = "2019-11-06", id = "singletonSubmissionEnvelope File", author = "alexie.staffer@ebi.ac.uk")
    public void singletonSubmissionEnvelopeFile(MongoDatabase db) {
        Document filter = Document.parse("{submissionEnvelopes: {$exists: 1}}");
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ submissionEnvelope: { $arrayElemAt: [ \"$submissionEnvelopes\", 0 ] } }")));
        update.add(new Document("$unset", "submissionEnvelopes"));

        db.getCollection("file").updateMany(filter, update);
    }

    @ChangeSet(order = "2019-11-07", id = "singletonSubmissionEnvelope Project", author = "alexie.staffer@ebi.ac.uk")
    public void singletonSubmissionEnvelopeProject(MongoDatabase db) {
        Document filter = Document.parse("{submissionEnvelopes: {$exists: 1}}");
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ submissionEnvelope: { $arrayElemAt: [ \"$submissionEnvelopes\", 0 ] } }")));

        db.getCollection("project").updateMany(filter, update);
    }

    @ChangeSet(order = "2020-08-11", id = "Drop Alias Index on archiveEntity", author = "karoly@ebi.ac.uk")
    public void dropAliasIndexOnArchiveEntity(MongoDatabase db) {
        try {
            db.getCollection("archiveEntity").dropIndex("alias");
            // If the collection does not exist this code will still succeed,
            // Which is good because we may change the collection name soon.
        } catch (MongoCommandException e) {
            if (!MONGO_INDEX_NOT_FOUND.equals(e.getErrorCode())) throw e;
            LOGGER.info(e.getErrorMessage());
        }
    }

    @ChangeSet(order = "2020-09-15", id = "singelton project.dataAccess.type", author = "alexie.staffer@ebi.ac.uk")
    public void singeltonProjectDataAccessType(MongoDatabase db) {
        Document filter = Document.parse("{'dataAccess.type': {$type: 'array'}}");
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ 'dataAccess.type': { $arrayElemAt: [ '$dataAccess.type', 0 ] } }")));
        db.getCollection("project").updateMany(filter, update);
    }

    @ChangeSet(order = "2021-05-20", id = "set default publications info", author = "alexie.staffer@ebi.ac.uk")
    public void setDefaultPublicationsInfo(MongoDatabase db) {
        Document filter = Document.parse("{ }");
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ 'publicationsInfo': [] }")));
        db.getCollection("project").updateMany(filter, update);
    }

    @ChangeSet(order = "2021-05-27", id = "Set default isInCatalogue", author = "alexie.staffer@ebi.ac.uk")
    public void setDefaultIsInCatalogue(MongoDatabase db) {
        Document filter = Document.parse("{ }");
        List<Document> update = new ArrayList<>();
        update.add(new Document("$unset", "publishedToCatalogue"));
        update.add(new Document("$set", Document.parse("{ 'isInCatalogue': false }")));
        db.getCollection("project").updateMany(filter, update);
    }

    @ChangeSet(order = "2021-07-16", id = "Add index to project", author = "jcbwndsr@ebi.ac.uk")
    public void addIndexToProject(MongoDatabase db) {
        Document indexQuery = Document.parse("{" +
                "'content.project_core.project_title': 'text'," +
                "'content.project_core.project_short_name': 'text'," +
                "'content.project_core.project_description': 'text'," +
                "'content.publications.authors': 'text'," +
                "'content.publications.title': 'text'," +
                "'content.publications.doi': 'text'," +
                "'content.contributors.name': 'text'," +
                "'content.insdc_project_accessions': 'text'," +
                "'content.ega_accessions': 'text'," +
                "'content.dbgap_accessions': 'text'," +
                "'content.geo_series_accessions': 'text'," +
                "'content.array_express_accessions': 'text'," +
                "'content.insdc_study_accessions': 'text'," +
                "'content.biostudies_accessions': 'text'," +
                "'technology.ontologies.ontology': 'text'," +
                "'technology.ontologies.ontology_label': 'text'," +
                "'organ.ontologies.ontology': 'text'," +
                "'organ.ontologies.ontology_label': 'text'," +
                "}");
        db.getCollection("project").createIndex(indexQuery);
    }

    @ChangeSet(order = "2021-11-22", id = "Set empty graphValidationErrors", author = "jcbwndsr@ebi.ac.uk")
    public void setGraphValidationErrors(MongoDatabase db) {
        Document filter = Document.parse("{ }");
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ 'graphValidationErrors': [] }")));
        db.getCollection("biomaterial").updateMany(filter, update);
        db.getCollection("process").updateMany(filter, update);
        db.getCollection("protocol").updateMany(filter, update);
        db.getCollection("file").updateMany(filter, update);
    }

    @ChangeSet(order = "2021-12-07", id = "Rename submission states", author = "jcbwndsr@ebi.ac.uk")
    public void renameSubmissionStates(MongoDatabase db) {
        Document filter = Document.parse("{ 'submissionState': 'VALID' }");
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ 'submissionState': 'METADATA_VALID' }")));
        db.getCollection("submissionEnvelope").updateMany(filter, update);

        filter = Document.parse("{ 'submissionState': 'VALIDATING' }");
        update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ 'submissionState': 'METADATA_VALIDATING' }")));
        db.getCollection("submissionEnvelope").updateMany(filter, update);

        filter = Document.parse("{ 'submissionState': 'INVALID' }");
        update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ 'submissionState': 'METADATA_INVALID' }")));
        db.getCollection("submissionEnvelope").updateMany(filter, update);
    }

    @ChangeSet(order = "2022-05-06",
               id = "add missing dataFileUuid for File documents with a unique uuid. dcp-764",
               author = "amnon@ebi.ac.uk")
    public void addMissingDataFileUuidToFiles(MongoDatabase db) {
        MongoCollection<Document> files = db.getCollection("file");
        files.find(eq("dataFileUuid", null))
             .forEach((Consumer<? super Document>) (Document file) ->
                files
                    .updateOne(eq("_id", file.get("_id")),
                               Updates.set("dataFileUuid", UUID.randomUUID())));
    }

    public void addSubmissionEnvelopeIndexToProcess(MongoDatabase db) {
        db.getCollection("process").createIndex(
                Document.parse("{ \"submissionEnvelope\": 1 }")
        );
    }
}

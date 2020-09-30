package org.humancellatlas.ingest.migrations;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
}

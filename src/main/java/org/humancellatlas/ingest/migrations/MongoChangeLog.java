package org.humancellatlas.ingest.migrations;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

@ChangeLog
public class MongoChangeLog {
    @ChangeSet(order = "2019-10-30", id = "featureCompatibilityVersion 3.4", author = "alexie.staffer@ebi.ac.uk")
    public void featureCompatibilityThreeFour(MongoDatabase db) {
        db.runCommand(new Document("setFeatureCompatibilityVersion", "3.4"));
    }

    @ChangeSet(order = "2019-10-31", id = "featureCompatibilityVersion 3.6", author = "alexie.staffer@ebi.ac.uk")
    public void featureCompatibilityThreeSix(MongoDatabase db) {
        db.runCommand(new Document("setFeatureCompatibilityVersion", "3.6"));
    }

    @ChangeSet(order = "2019-11-01", id = "featureCompatibilityVersion 4.0", author = "alexie.staffer@ebi.ac.uk")
    public void featureCompatibilityFourZero(MongoDatabase db) {
        db.runCommand(new Document("setFeatureCompatibilityVersion", "4.0"));
        db.runCommand(new Document("setFreeMonitoring", 1).append("action", "disable"));
    }

    @ChangeSet(order = "2019-11-02", id = "featureCompatibilityVersion 4.2", author = "alexie.staffer@ebi.ac.uk")
    public void featureCompatibilityFourTwo(MongoDatabase db) {
        db.runCommand(new Document("setFeatureCompatibilityVersion", "4.2"));
    }

    @ChangeSet(order = "2019-11-03", id = "singletonSubmissionEnvelope Biomaterial", author = "alexie.staffer@ebi.ac.uk")
    public void singletonSubmissionEnvelopeBiomaterial(MongoDatabase db) {
        Document filter = new Document();
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ submissionEnvelope: { $arrayElemAt: [ \"$submissionEnvelopes\", 0 ] } }")));
        update.add(new Document("$unset", "submissionEnvelopes"));

        db.getCollection("biomaterial").updateMany(filter, update);
    }

    @ChangeSet(order = "2019-11-04", id = "singletonSubmissionEnvelope Process", author = "alexie.staffer@ebi.ac.uk")
    public void singletonSubmissionEnvelopeProcess(MongoDatabase db) {
        Document filter = new Document();
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ submissionEnvelope: { $arrayElemAt: [ \"$submissionEnvelopes\", 0 ] } }")));
        update.add(new Document("$unset", "submissionEnvelopes"));

        db.getCollection("process").updateMany(filter, update);
    }

    @ChangeSet(order = "2019-11-05", id = "singletonSubmissionEnvelope Protocol", author = "alexie.staffer@ebi.ac.uk")
    public void singletonSubmissionEnvelopeProtocol(MongoDatabase db) {
        Document filter = new Document();
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ submissionEnvelope: { $arrayElemAt: [ \"$submissionEnvelopes\", 0 ] } }")));
        update.add(new Document("$unset", "submissionEnvelopes"));

        db.getCollection("protocol").updateMany(filter, update);
    }

    @ChangeSet(order = "2019-11-06", id = "singletonSubmissionEnvelope File", author = "alexie.staffer@ebi.ac.uk")
    public void singletonSubmissionEnvelopeFile(MongoDatabase db) {
        Document filter = new Document();
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ submissionEnvelope: { $arrayElemAt: [ \"$submissionEnvelopes\", 0 ] } }")));
        update.add(new Document("$unset", "submissionEnvelopes"));

        db.getCollection("file").updateMany(filter, update);
    }

    @ChangeSet(order = "2019-11-07", id = "singletonSubmissionEnvelope Project", author = "alexie.staffer@ebi.ac.uk")
    public void singletonSubmissionEnvelopeProject(MongoDatabase db) {
        Document filter = new Document();
        List<Document> update = new ArrayList<>();
        update.add(new Document("$set", Document.parse("{ submissionEnvelope: { $arrayElemAt: [ \"$submissionEnvelopes\", 0 ] } }")));

        db.getCollection("project").updateMany(filter, update);
    }
}

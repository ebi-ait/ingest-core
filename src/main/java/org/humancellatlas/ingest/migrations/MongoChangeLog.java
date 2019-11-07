package org.humancellatlas.ingest.migrations;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

@ChangeLog
public class MongoChangeLog {
    @ChangeSet(order = "2019-10-30", id="featureCompatibilityVersion 3.4", author = "alexie.staffer@ebi.ac.uk")
    public void featureCompatibilityThreeFour(MongoDatabase db) {
        db.runCommand( new Document("setFeatureCompatibilityVersion", "3.4") );
    }

    @ChangeSet(order = "2019-10-31", id="featureCompatibilityVersion 3.6", author = "alexie.staffer@ebi.ac.uk")
    public void featureCompatibilityThreeSix(MongoDatabase db) {
        db.runCommand( new Document("setFeatureCompatibilityVersion", "3.6") );
    }

    @ChangeSet(order = "2019-11-01", id="featureCompatibilityVersion 4.0", author = "alexie.staffer@ebi.ac.uk")
    public void featureCompatibilityFourZero(MongoDatabase db) {
        db.runCommand( new Document("setFeatureCompatibilityVersion", "4.0") );
        db.runCommand( new Document("setFreeMonitoring", 1).append("action", "disable") );
    }
}

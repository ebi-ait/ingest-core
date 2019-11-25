package org.humancellatlas.ingest.migrations;

import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.ServerVersion;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

class MongoVersionHelper {
    private static ServerVersion getVersionFromString(String version) {
        List<Integer> numberList = new ArrayList<>();
        for(String number : version.split("\\.")) {
            numberList.add(Integer.parseInt(number));
        }
        while (numberList.size() < 3) {
            numberList.add(0);
        }
        return new ServerVersion(numberList);
    }
    private static ServerVersion getMajorMinor(ServerVersion version) {
        List<Integer> list = new ArrayList<>();
        list.add(version.getVersionList().get(0));
        list.add(version.getVersionList().get(1));
        list.add(0);
        return new ServerVersion(list);
    }

    private static String getMajorMinorString(ServerVersion version) {
        String major = version.getVersionList().get(0).toString();
        String minor = version.getVersionList().get(1).toString();
        return major + "." + minor;
    }

    static ServerVersion getFeatureCompatibilityVersion(MongoDatabase db) {
        Document response = db.runCommand(new Document("getParameter", 1).append("featureCompatibilityVersion", 1));
        if (response.containsKey("ok") && response.containsKey("featureCompatibilityVersion")) {
            if (getServerVersion(db).compareTo(getVersionFromString("3.6")) < 0)
                return  getVersionFromString(response.getString("featureCompatibilityVersion"));
            else {
                Document featureCompatibilityVersion = response.get("featureCompatibilityVersion", Document.class);
                if (featureCompatibilityVersion.containsKey("version"))
                    return getVersionFromString(featureCompatibilityVersion.getString("version"));
            }
        }
        throw new UnsupportedOperationException("Could not retrieve featureCompatibilityVersion.");
    }

    static ServerVersion getServerVersion(MongoDatabase db) {
        Document server_doc = db.runCommand(new Document("buildinfo", 1));
        if (server_doc.containsKey("ok") && server_doc.containsKey("version")) {
            return getVersionFromString(server_doc.getString("version"));
        }
        throw new UnsupportedOperationException("Could not retrieve server version.");
    }

    static Boolean featureCompatibilityLessThan(MongoDatabase db, String version) {
        return (getFeatureCompatibilityVersion(db).compareTo(getVersionFromString(version)) < 0);
    }
}

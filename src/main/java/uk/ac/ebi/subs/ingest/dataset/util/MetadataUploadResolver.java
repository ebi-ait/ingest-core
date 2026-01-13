package uk.ac.ebi.subs.ingest.dataset.util;

import org.bson.Document;
import uk.ac.ebi.subs.ingest.dataset.Dataset;

import java.util.List;
import java.util.Map;

import static uk.ac.ebi.subs.ingest.dataset.util.ContentMaps.asMap;

public final class MetadataUploadResolver {
    private MetadataUploadResolver() {}

    @SuppressWarnings("unchecked")
    private static Map<String, Object> metadataUpload(Dataset d) {
        Map<String, Object> content = asMap(d.getContent());
        Object mu = content.get("metadataUpload");
        if (!(mu instanceof Map)) return null;
        return (Map<String, Object>) mu;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMapSafe(Object o) {
        if (o instanceof Map) return (Map<String, Object>) o;
        if (o instanceof Document) return (Document) o;
        return null;
    }

    private static Map<String, Object> resolveCurrentAttempt(Map<String, Object> mu) {
        if (mu == null) return null;

        Object historyObj = mu.get("history");
        if (!(historyObj instanceof List)) return null;

        List<?> history = (List<?>) historyObj;
        if (history.isEmpty()) return null;

        String currentAttemptId = str(mu.get("currentAttemptId"));

        if (currentAttemptId != null) {
            for (Object o : history) {
                Map<String, Object> m = asMapSafe(o);
                if (m == null) continue;
                if (currentAttemptId.equals(str(m.get("attemptId")))) return m;
            }
        }

        for (Object o : history) {
            Map<String, Object> m = asMapSafe(o);
            if (m != null) return m;
        }

        return null;
    }

    public static String currentKey(Dataset d) {
        Map<String, Object> mu = metadataUpload(d);
        Map<String, Object> attempt = resolveCurrentAttempt(mu);
        return attempt == null ? null : str(attempt.get("key"));
    }

    public static String currentStatus(Dataset d) {
        Map<String, Object> mu = metadataUpload(d);
        Map<String, Object> attempt = resolveCurrentAttempt(mu);
        return attempt == null ? null : str(attempt.get("status"));
    }

    public static String currentAttemptId(Dataset d) {
        Map<String, Object> mu = metadataUpload(d);
        Map<String, Object> attempt = resolveCurrentAttempt(mu);
        return attempt == null ? null : str(attempt.get("attemptId"));
    }

    public static String currentETag(Dataset d) {
        Map<String, Object> mu = metadataUpload(d);
        Map<String, Object> attempt = resolveCurrentAttempt(mu);
        return attempt == null ? null : str(attempt.get("etag"));
    }

    private static String str(Object v) { return v == null ? null : String.valueOf(v); }
}

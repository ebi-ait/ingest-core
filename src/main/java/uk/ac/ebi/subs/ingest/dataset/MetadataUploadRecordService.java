package uk.ac.ebi.subs.ingest.dataset;

import static uk.ac.ebi.subs.ingest.dataset.util.ContentMaps.asMap;
import static uk.ac.ebi.subs.ingest.dataset.util.ContentMaps.getOrCreateList;
import static uk.ac.ebi.subs.ingest.dataset.util.ContentMaps.getOrCreateMap;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.services.s3.model.ObjectMetadata;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetadataUploadRecordService {

  private final DatasetRepository datasetRepository;

  private static final int MAX_HISTORY = 20;

  public MetadataUploadRecord confirmUpload(
      String datasetId,
      String bucket,
      String key,
      ObjectMetadata meta,
      UploadValidationRules rules,
      String confirmedBy // audit
      ) {
    Dataset dataset =
        datasetRepository
            .findById(datasetId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "dataset not found"));

    String etag = meta.getETag();
    long sizeBytes = meta.getContentLength();
    String contentType = meta.getContentType();
    Instant now = Instant.now();

    ValidationOutcome outcome = validate(meta, rules);

    Map<String, Object> content = asMap(dataset.getContent());
    Map<String, Object> metadataUpload = getOrCreateMap(content, "metadataUpload");
    List<Object> history = getOrCreateList(metadataUpload, "history");

    String attemptId = UUID.randomUUID().toString();

    Map<String, Object> attempt = new LinkedHashMap<>();
    attempt.put("attemptId", attemptId);
    attempt.put("key", key);
    attempt.put("etag", etag);
    attempt.put("sizeBytes", sizeBytes);
    attempt.put("contentType", contentType);
    attempt.put("uploadedAt", now.toString());
    attempt.put("status", outcome.status.name());
    attempt.put("message", outcome.message);
    if (confirmedBy != null) attempt.put("confirmedBy", confirmedBy);

    history.add(0, attempt);
    trimHistory(history);

    metadataUpload.put("currentAttemptId", attemptId);

    dataset.setContent(content);
    datasetRepository.save(dataset);

    return new MetadataUploadRecord(
        datasetId,
        attemptId,
        outcome.status,
        key,
        etag,
        sizeBytes,
        contentType,
        now.toString(),
        outcome.message);
  }

  public MetadataUploadRecord recordFailure(
      String datasetId,
      String bucket,
      String key,
      String message,
      String confirmedBy // audit (still useful)
      ) {
    Dataset dataset =
        datasetRepository
            .findById(datasetId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "dataset not found"));

    Instant now = Instant.now();
    String attemptId = UUID.randomUUID().toString();

    Map<String, Object> content = asMap(dataset.getContent());
    Map<String, Object> metadataUpload = getOrCreateMap(content, "metadataUpload");
    List<Object> history = getOrCreateList(metadataUpload, "history");

    Map<String, Object> attempt = new LinkedHashMap<>();
    attempt.put("attemptId", attemptId);
    attempt.put("key", key);
    attempt.put("etag", null);
    attempt.put("sizeBytes", 0L);
    attempt.put("contentType", null);
    attempt.put("uploadedAt", now.toString());
    attempt.put("status", Status.FAILED.name());
    attempt.put("message", message);
    if (confirmedBy != null) attempt.put("confirmedBy", confirmedBy);

    history.add(0, attempt);
    trimHistory(history);

    metadataUpload.put("currentAttemptId", attemptId);

    dataset.setContent(content);
    datasetRepository.save(dataset);

    return new MetadataUploadRecord(
        datasetId, attemptId, Status.FAILED, key, null, 0L, null, now.toString(), message);
  }

  public MetadataUploadRecord latest(String datasetId) {
    Dataset dataset =
        datasetRepository
            .findById(datasetId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "dataset not found"));

    Map<String, Object> content = asMap(dataset.getContent());
    Object muObj = content.get("metadataUpload");
    if (!(muObj instanceof Map)) return null;

    @SuppressWarnings("unchecked")
    Map<String, Object> metadataUpload = (Map<String, Object>) muObj;

    Map<String, Object> attempt = resolveCurrentAttempt(metadataUpload);
    if (attempt == null) return null;

    Status status = safeStatus(str(attempt.get("status")));

    return new MetadataUploadRecord(
        datasetId,
        str(attempt.get("attemptId")),
        status,
        str(attempt.get("key")),
        str(attempt.get("etag")),
        longVal(attempt.get("sizeBytes")),
        str(attempt.get("contentType")),
        str(attempt.get("uploadedAt")),
        str(attempt.get("message")));
  }

  // -------------------------
  // Validation
  // -------------------------

  private ValidationOutcome validate(ObjectMetadata meta, UploadValidationRules rules) {
    if (rules == null) rules = UploadValidationRules.defaults();

    long size = meta.getContentLength();
    String ct = meta.getContentType();

    if (rules.requiredContentType != null) {
      if (ct == null || !ct.equalsIgnoreCase(rules.requiredContentType)) {
        return new ValidationOutcome(
            Status.FAILED,
            "Invalid contentType. Expected " + rules.requiredContentType + ", got " + ct);
      }
    }

    if (size <= 0) {
      return new ValidationOutcome(Status.FAILED, "Empty object (sizeBytes=0)");
    }

    if (rules.minSizeBytes > 0 && size < rules.minSizeBytes) {
      return new ValidationOutcome(
          Status.FAILED,
          "Object too small (" + size + " bytes). Minimum is " + rules.minSizeBytes + " bytes");
    }

    return new ValidationOutcome(Status.UPLOADED_CONFIRMED, "Staging object confirmed");
  }

  public static class UploadValidationRules {
    public final String requiredContentType;
    public final long minSizeBytes;

    public UploadValidationRules(String requiredContentType, long minSizeBytes) {
      this.requiredContentType = requiredContentType;
      this.minSizeBytes = minSizeBytes;
    }

    public static UploadValidationRules defaults() {
      return new UploadValidationRules(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 1024L);
    }
  }

  private static class ValidationOutcome {
    final Status status;
    final String message;

    ValidationOutcome(Status status, String message) {
      this.status = status;
      this.message = message;
    }
  }

  // -------------------------
  // Internals / helpers
  // -------------------------

  private static void trimHistory(List<Object> history) {
    while (history.size() > MAX_HISTORY) {
      history.remove(history.size() - 1);
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> asMapSafe(Object o) {
    if (o instanceof Map) return (Map<String, Object>) o;
    if (o instanceof Document) return (Document) o;
    return null;
  }

  private static Map<String, Object> resolveCurrentAttempt(Map<String, Object> metadataUpload) {
    Object historyObj = metadataUpload.get("history");
    if (!(historyObj instanceof List)) return null;

    List<?> history = (List<?>) historyObj;
    if (history.isEmpty()) return null;

    String currentAttemptId = str(metadataUpload.get("currentAttemptId"));

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

  private static String str(Object v) {
    return v == null ? null : String.valueOf(v);
  }

  private static long longVal(Object v) {
    if (v == null) return 0L;
    if (v instanceof Number) return ((Number) v).longValue();
    try {
      return Long.parseLong(String.valueOf(v));
    } catch (Exception e) {
      return 0L;
    }
  }

  private static Status safeStatus(String s) {
    if (s == null) return Status.UNKNOWN;
    try {
      return Status.valueOf(s);
    } catch (Exception e) {
      return Status.UNKNOWN;
    }
  }

  // -------------------------
  // DTO returned to controllers
  // -------------------------

  public static class MetadataUploadRecord {
    private final String datasetId;
    private final String attemptId;
    private final Status status;
    private final String key;
    private final String eTag;
    private final long sizeBytes;
    private final String contentType;
    private final String uploadedAt;
    private final String message;

    public MetadataUploadRecord(
        String datasetId,
        String attemptId,
        Status status,
        String key,
        String eTag,
        long sizeBytes,
        String contentType,
        String uploadedAt,
        String message) {
      this.datasetId = datasetId;
      this.attemptId = attemptId;
      this.status = status;
      this.key = key;
      this.eTag = eTag;
      this.sizeBytes = sizeBytes;
      this.contentType = contentType;
      this.uploadedAt = uploadedAt;
      this.message = message;
    }

    public String getDatasetId() {
      return datasetId;
    }

    public String getAttemptId() {
      return attemptId;
    }

    public Status getStatus() {
      return status;
    }

    public String getKey() {
      return key;
    }

    public String getETag() {
      return eTag;
    }

    public long getSizeBytes() {
      return sizeBytes;
    }

    public String getContentType() {
      return contentType;
    }

    public String getUploadedAt() {
      return uploadedAt;
    }

    public String getMessage() {
      return message;
    }
  }

  public enum Status {
    UPLOADED_CONFIRMED,
    FAILED,
    UNKNOWN
  }
}

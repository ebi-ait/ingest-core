package uk.ac.ebi.subs.ingest.dataset;

import static uk.ac.ebi.subs.ingest.dataset.util.ContentMaps.asMap;
import static uk.ac.ebi.subs.ingest.dataset.util.ContentMaps.getOrCreateList;
import static uk.ac.ebi.subs.ingest.dataset.util.ContentMaps.getOrCreateMap;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.services.s3.model.ObjectMetadata;

import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.dataset.util.S3StagingService;

@Service
@RequiredArgsConstructor
public class MetadataUploadRecordService {

  private final DatasetRepository datasetRepository;
  private final S3StagingService s3StagingService;

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

    ValidationOutcome outcome = validate(datasetId, bucket, key, meta, rules);

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
    attempt.put("zipMagicOk", outcome.zipMagicOk);
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
        outcome.message,
        outcome.zipMagicOk);
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
        datasetId, attemptId, Status.FAILED, key, null, 0L, null, now.toString(), message, null);
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

    Boolean zipMagicOk = null;
    if (attempt.containsKey("zipMagicOk")) {
      Object v = attempt.get("zipMagicOk");
      if (v instanceof Boolean) {
        zipMagicOk = (Boolean) v;
      } else if (v != null) {
        // defensive: if stored as string somehow
        zipMagicOk = Boolean.valueOf(String.valueOf(v));
      }
    }

    return new MetadataUploadRecord(
        datasetId,
        str(attempt.get("attemptId")),
        status,
        str(attempt.get("key")),
        str(attempt.get("etag")),
        longVal(attempt.get("sizeBytes")),
        str(attempt.get("contentType")),
        str(attempt.get("uploadedAt")),
        str(attempt.get("message")),
        zipMagicOk);
  }

  // -------------------------
  // Validation
  // -------------------------

  private ValidationOutcome validate(
      String datasetId,
      String bucket,
      String key,
      ObjectMetadata meta,
      UploadValidationRules rules) {

    if (rules == null) rules = UploadValidationRules.defaults();

    // ---- bucket: do not trust caller-supplied bucket
    String expectedBucket = s3StagingService.bucket();
    if (bucket != null && !bucket.isBlank() && !expectedBucket.equals(bucket)) {
      return new ValidationOutcome(
          Status.FAILED, "Invalid bucket. Expected " + expectedBucket + ", got " + bucket, false);
    }

    // ---- key ownership / tenancy
    String prefix = datasetId + "/metadata/";
    if (key == null || !key.startsWith(prefix)) {
      return new ValidationOutcome(Status.FAILED, "Invalid key: not under " + prefix, false);
    }

    if (rules.requireXlsxSuffix && !key.endsWith(".xlsx")) {
      return new ValidationOutcome(Status.FAILED, "Invalid key: must end with .xlsx", false);
    }

    if (rules.enforceTimestampPattern) {
      // Expect: <datasetId>/metadata/yyyyMMdd-HHmmss-SSS.xlsx
      String re = "^" + Pattern.quote(datasetId) + "/metadata/\\d{8}-\\d{6}-\\d{3}\\.xlsx$";
      if (!key.matches(re)) {
        return new ValidationOutcome(
            Status.FAILED,
            "Invalid key: must match " + datasetId + "/metadata/yyyyMMdd-HHmmss-SSS.xlsx",
            false);
      }
    }

    // ---- content-type (normalized)
    String ct = baseContentType(meta.getContentType());
    if (rules.requiredContentType != null) {
      String expectedCt = baseContentType(rules.requiredContentType);
      if (ct == null || !ct.equalsIgnoreCase(expectedCt)) {
        return new ValidationOutcome(
            Status.FAILED,
            "Invalid contentType. Expected "
                + rules.requiredContentType
                + ", got "
                + meta.getContentType(),
            false);
      }
    }

    // ---- size checks
    long size = meta.getContentLength();
    if (size <= 0) {
      return new ValidationOutcome(Status.FAILED, "Empty object (sizeBytes=0)", false);
    }

    if (rules.minSizeBytes > 0 && size < rules.minSizeBytes) {
      return new ValidationOutcome(
          Status.FAILED,
          "File is too small ("
              + humanBytes(size)
              + "). Minimum allowed size is "
              + humanBytes(rules.minSizeBytes)
              + ".",
          null);
    }

    if (rules.maxSizeBytes > 0 && size > rules.maxSizeBytes) {
      return new ValidationOutcome(
          Status.FAILED,
          "File is too large ("
              + humanBytes(size)
              + "). Maximum allowed size is "
              + humanBytes(rules.maxSizeBytes)
              + ".",
          null);
    }

    // ---- ZIP magic bytes check (Range GET 0-3)
    boolean zipMagicOk = false;
    if (rules.requireZipMagic) {
      byte[] b = s3StagingService.readFirstBytes(key, 4);
      zipMagicOk =
          b.length == 4
              && (b[0] == 0x50) // 'P'
              && (b[1] == 0x4B) // 'K'
              && (b[2] == 0x03)
              && (b[3] == 0x04);

      if (!zipMagicOk) {
        return new ValidationOutcome(
            Status.FAILED, "File does not look like an XLSX (ZIP header missing)", false);
      }
    }

    return new ValidationOutcome(
        Status.UPLOADED_CONFIRMED,
        "Staging object confirmed",
        rules.requireZipMagic ? zipMagicOk : null);
  }

  private static String baseContentType(String ct) {
    if (ct == null) return null;
    int semi = ct.indexOf(';');
    return (semi >= 0 ? ct.substring(0, semi) : ct).trim();
  }

  public static class UploadValidationRules {
    public final String requiredContentType;
    public final long minSizeBytes;
    public final long maxSizeBytes;

    public final boolean requireXlsxSuffix;
    public final boolean requireZipMagic;
    public final boolean enforceTimestampPattern;

    public UploadValidationRules(
        String requiredContentType,
        long minSizeBytes,
        long maxSizeBytes,
        boolean requireXlsxSuffix,
        boolean requireZipMagic,
        boolean enforceTimestampPattern) {
      this.requiredContentType = requiredContentType;
      this.minSizeBytes = minSizeBytes;
      this.maxSizeBytes = maxSizeBytes;
      this.requireXlsxSuffix = requireXlsxSuffix;
      this.requireZipMagic = requireZipMagic;
      this.enforceTimestampPattern = enforceTimestampPattern;
    }

    public static UploadValidationRules defaults() {
      return new UploadValidationRules(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          1024L,
          50L * 1024L * 1024L, // 50MB
          true,
          true,
          false // optional; enable if strict timestamp enforcement is needed
          );
    }
  }

  private static class ValidationOutcome {
    final Status status;
    final String message;
    final Boolean zipMagicOk;

    ValidationOutcome(Status status, String message, Boolean zipMagicOk) {
      this.status = status;
      this.message = message;
      this.zipMagicOk = zipMagicOk;
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
    private final Boolean zipMagicOk;

    public MetadataUploadRecord(
        String datasetId,
        String attemptId,
        Status status,
        String key,
        String eTag,
        long sizeBytes,
        String contentType,
        String uploadedAt,
        String message,
        Boolean zipMagicOk) {
      this.datasetId = datasetId;
      this.attemptId = attemptId;
      this.status = status;
      this.key = key;
      this.eTag = eTag;
      this.sizeBytes = sizeBytes;
      this.contentType = contentType;
      this.uploadedAt = uploadedAt;
      this.message = message;
      this.zipMagicOk = zipMagicOk;
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

    public Boolean getZipMagicOk() {
      return zipMagicOk;
    }
  }

  public enum Status {
    UPLOADED_CONFIRMED,
    FAILED,
    UNKNOWN
  }

  private static String humanBytes(long bytes) {
    if (bytes < 1024) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    String pre = "KMGTPE".charAt(exp - 1) + "B";
    return String.format("%.0f %s", bytes / Math.pow(1024, exp), pre);
  }
}

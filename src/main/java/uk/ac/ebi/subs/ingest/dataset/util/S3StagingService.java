package uk.ac.ebi.subs.ingest.dataset.util;

import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

import lombok.Value;

@Service
public class S3StagingService {

  private static final String XLSX_CONTENT_TYPE =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  private final AmazonS3 s3;
  private final S3StagingProperties props;

  private static final DateTimeFormatter TS =
      DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS").withZone(ZoneOffset.UTC);

  public S3StagingService(AmazonS3 s3, S3StagingProperties props) {
    this.s3 = s3;
    this.props = props;
  }

  public String bucket() {
    if (props.getBucket() == null || props.getBucket().isBlank()) {
      throw new IllegalStateException("morphic.staging.bucket is not configured");
    }
    return props.getBucket();
  }

  /** Generates a unique key per upload attempt (reupload-safe). */
  public String buildMetadataKey(String datasetId) {
    String ts = TS.format(Instant.now());
    return datasetId + "/metadata/" + ts + ".xlsx";
  }

  public PresignedUpload presignMetadataUpload(String datasetId) {
    String bucket = bucket();
    String key = buildMetadataKey(datasetId);

    Date expiry = Date.from(Instant.now().plusSeconds(props.getPresignExpirySeconds()));

    GeneratePresignedUrlRequest req =
        new GeneratePresignedUrlRequest(bucket, key)
            .withMethod(HttpMethod.PUT)
            .withExpiration(expiry);

    // Enforce Content-Type (client must send same header)
    req.addRequestParameter("Content-Type", XLSX_CONTENT_TYPE);

    URL url = s3.generatePresignedUrl(req);

    return new PresignedUpload(
        bucket,
        key,
        url.toString(),
        props.getPresignExpirySeconds(),
        Map.of("Content-Type", XLSX_CONTENT_TYPE));
  }

  @Value
  public static class PresignedUpload {
    String bucket;
    String key;
    String uploadUrl;
    int expiresInSeconds;
    Map<String, String> signedHeaders;
  }

  /** HEAD any key, not only "metadata.xlsx". */
  public ObjectMetadata headObject(String key) {
    String bucket = bucket();
    try {
      return s3.getObjectMetadata(bucket, key);
    } catch (com.amazonaws.services.s3.model.AmazonS3Exception e) {
      if (e.getStatusCode() == 404) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "object not found in staging");
      }
      throw e;
    }
  }
}

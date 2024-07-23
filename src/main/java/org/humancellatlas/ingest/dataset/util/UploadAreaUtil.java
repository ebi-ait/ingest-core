package org.humancellatlas.ingest.dataset.util;

import org.humancellatlas.ingest.dataset.Dataset;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UploadAreaUtil {
  public void createDataFilesUploadArea(final Dataset dataset) {
    final String datasetId = dataset.getId();

    try {
      final AmazonS3 s3Client =
          AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();

      if (!s3Client.doesBucketExistV2(datasetId)) {
        s3Client.createBucket(new CreateBucketRequest(datasetId, Region.EU_London));
        dataset.setComment(
            "Upload area created for this dataset with name "
                + dataset.getId()
                + " Please use the morphic-util tool to upload your data files to the dataset upload area");
      } else {
        log.info("Bucket already exists for dataset with ID " + datasetId);

        dataset.setComment(
            "Upload area available for this dataset with name "
                + dataset.getId()
                + " Please use the morphic-util tool to upload your data files to the dataset upload area");
      }
    } catch (AmazonServiceException ase) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "AmazonServiceException: " + ase.getMessage());
    } catch (AmazonClientException ace) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "AmazonClientException: " + ace.getMessage());
    }
  }

  public void deleteDataFilesAndUploadArea(final String datasetId) {
    try {
      final AmazonS3 s3Client =
          AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();

      if (s3Client.doesBucketExistV2(datasetId)) {
        // List and delete all objects in the bucket
        ListObjectsV2Request listObjectsV2Request =
            new ListObjectsV2Request().withBucketName(datasetId);
        ListObjectsV2Result result;
        do {
          result = s3Client.listObjectsV2(listObjectsV2Request);

          for (final S3ObjectSummary objectSummary : result.getObjectSummaries()) {
            s3Client.deleteObject(new DeleteObjectRequest(datasetId, objectSummary.getKey()));
          }
          listObjectsV2Request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());

        // Delete the bucket
        s3Client.deleteBucket(datasetId);
        log.info("Bucket and all contents deleted for dataset with ID " + datasetId);
      } else {
        log.info("Bucket does not exist for dataset with ID " + datasetId);
      }
    } catch (AmazonServiceException ase) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "AmazonServiceException: " + ase.getMessage());
    } catch (AmazonClientException ace) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "AmazonClientException: " + ace.getMessage());
    }
  }
}

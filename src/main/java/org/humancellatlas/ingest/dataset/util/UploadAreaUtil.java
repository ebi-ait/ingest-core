package org.humancellatlas.ingest.dataset.util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.Region;
import lombok.extern.slf4j.Slf4j;
import org.humancellatlas.ingest.dataset.Dataset;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class UploadAreaUtil {
    public void createDataFilesUploadArea(final Dataset dataset) {
        final String datasetId = dataset.getId();

        try {
            final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(Regions.EU_WEST_2)
                    .build();

            if (!s3Client.doesBucketExistV2(datasetId)) {
                s3Client.createBucket(new CreateBucketRequest(datasetId, Region.EU_London));
                dataset.setComment("Upload area created for this dataset with name " + dataset.getId()
                        + " Please use the morphic-util tool to upload your data files to the dataset upload area");
            } else {
                log.info("Bucket already exists for dataset with ID " + datasetId);

                dataset.setComment("Upload area available for this dataset with name " + dataset.getId()
                        + " Please use the morphic-util tool to upload your data files to the dataset upload area");
            }
        } catch (AmazonServiceException ase) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "AmazonServiceException: " + ase.getMessage());
        } catch (AmazonClientException ace) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "AmazonClientException: " + ace.getMessage());
        }
    }
}

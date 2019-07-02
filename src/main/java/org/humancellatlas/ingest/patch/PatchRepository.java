package org.humancellatlas.ingest.patch;

import org.bson.types.ObjectId;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
public interface PatchRepository extends MongoRepository<Patch, String> {

    @RestResource(path="updatedocument", rel="WithUpdateDocument")
    @Query("{ 'updateDocument.$id': ?0 }")
    Patch<? extends MetadataDocument> findByUpdateDocumentId(ObjectId id);

}

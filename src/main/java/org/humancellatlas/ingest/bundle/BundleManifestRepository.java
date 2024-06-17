package org.humancellatlas.ingest.bundle;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

/** Created by rolando on 05/09/2017. */
@CrossOrigin
public interface BundleManifestRepository
    extends MongoRepository<BundleManifest, String>, BundleManifestRepositoryCustom {
  Page<BundleManifest> findByBundleUuid(@Param("uuid") String uuid, Pageable pageable);

  Optional<BundleManifest> findTopByBundleUuidOrderByBundleVersionDesc(String uuid);

  Page<BundleManifest> findByEnvelopeUuid(String uuid, Pageable pageable);

  Page<BundleManifest> findAll(Pageable pageable);

  Long deleteByEnvelopeUuid(String uuid);

  @RestResource(exported = false)
  Stream<BundleManifest> findByEnvelopeUuid(String envelopeUuid);
}

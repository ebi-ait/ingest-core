package uk.ac.ebi.subs.ingest.bundle;

import java.text.DecimalFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.EntityType;
import uk.ac.ebi.subs.ingest.core.MetadataDocument;

@Service
@RequiredArgsConstructor
public class BundleManifestService {
  private final @NonNull BundleManifestRepository bundleManifestRepository;
  private final Logger log = LoggerFactory.getLogger(getClass());

  public Map<String, Set<MetadataDocument>> bundleManifestsForDocuments(
      Collection<MetadataDocument> documents) {

    Map<String, Set<MetadataDocument>> hits = new HashMap<>();

    long fileStartTime = System.currentTimeMillis();
    Iterator<BundleManifest> iterator = allManifestsIterator();

    while (iterator.hasNext()) {
      BundleManifest bundleManifest = iterator.next();
      documents.forEach(
          document -> {
            String documentUuid = document.getUuid().getUuid().toString();
            String bundleUuid = bundleManifest.getBundleUuid();
            EntityType documentType = document.getType();
            entityMapFromManifest(documentType, bundleManifest)
                .ifPresent(
                    entityMap -> {
                      if (entityMap.containsKey(documentUuid)) {
                        if (hits.containsKey(bundleUuid)) {
                          hits.get(bundleUuid).add(document);
                        } else {
                          hits.put(bundleUuid, new HashSet<>(Collections.singletonList(document)));
                        }
                      }
                    });
          });
    }

    long fileEndTime = System.currentTimeMillis();
    float fileQueryTime = ((float) (fileEndTime - fileStartTime)) / 1000;
    String fileQt = new DecimalFormat("#,###.##").format(fileQueryTime);
    log.info("Finding bundles to update took {}s", fileQt);
    log.info("documentsToUpdate: {}, bundlesToUpdate:{}", documents.size(), hits.keySet().size());
    return hits;
  }

  private Iterator<BundleManifest> allManifestsIterator() {
    Iterator<BundleManifest> manifestsIterator =
        new Iterator<BundleManifest>() {
          Pageable pageable = new PageRequest(0, 5000);
          Page<BundleManifest> pagedBundleManifests = null;
          Queue<BundleManifest> bundleManifests;

          private void fetch(Pageable pageable) {
            pagedBundleManifests = bundleManifestRepository.findAll(pageable);
            bundleManifests = new LinkedList<>(pagedBundleManifests.getContent());
          }

          @Override
          public boolean hasNext() {
            return (pagedBundleManifests == null
                || bundleManifests.size() > 0
                || pagedBundleManifests.hasNext());
          }

          @Override
          public BundleManifest next() {
            BundleManifest bundleManifest = null;

            if (pagedBundleManifests == null) {
              fetch(pageable);
            }

            if (bundleManifests.size() == 0 && pagedBundleManifests.hasNext()) {
              fetch(pagedBundleManifests.nextPageable());
            }

            if (bundleManifests.size() > 0) {
              bundleManifest = bundleManifests.remove();
            }

            return bundleManifest;
          }
        };

    return manifestsIterator;
  }

  private Optional<Map<String, Collection<String>>> entityMapFromManifest(
      EntityType entityType, BundleManifest bundleManifest) {
    if (entityType.equals(EntityType.BIOMATERIAL)) {
      return Optional.ofNullable(bundleManifest.getFileBiomaterialMap());
    } else if (entityType.equals(EntityType.FILE)) {
      return Optional.ofNullable(bundleManifest.getFileFilesMap());
    } else if (entityType.equals(EntityType.PROTOCOL)) {
      return Optional.ofNullable(bundleManifest.getFileProtocolMap());
    } else if (entityType.equals(EntityType.PROCESS)) {
      return Optional.ofNullable(bundleManifest.getFileProcessMap());
    } else if (entityType.equals(EntityType.PROJECT)) {
      return Optional.ofNullable(bundleManifest.getFileProjectMap());
    } else {
      throw new RuntimeException(
          String.format(
              "Bundle manifest %s contains no entity map for entity type %s",
              bundleManifest.getId(), entityType.toString()));
    }
  }
}

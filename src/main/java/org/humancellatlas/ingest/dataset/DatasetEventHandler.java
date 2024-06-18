package org.humancellatlas.ingest.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatasetEventHandler {
  private final Logger log = LoggerFactory.getLogger(getClass());

  public void registeredDataset(Dataset dataset) {
    log.info("A new dataset [" + dataset.getUuid() + "] has been registered.");
  }

  public void updatedDataset(Dataset dataset) {
    log.info("Updated dataset: {}", dataset.getUuid());
  }

  public void deletedDataset(String id) {
    log.info("Deleted dataset with ID: {}", id);
  }
}

package uk.ac.ebi.subs.ingest.gene;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Lightweight client that turns a gene symbol (e.g. "BHLHE40") into its HGNC identifier
 * ("HGNC:1046") by calling the external Lambda endpoint defined in application-*.properties:
 *
 * <p>gene.api.base = https://46ucfedadd.execute-api.us-east-1.amazonaws.com/api
 */
@Slf4j
@Component
public class GeneSearchClient {

  private final String geneApiBase;
  private final RestTemplate rest;

  public GeneSearchClient(
      @Value("${gene.api.base}") String geneApiBase, RestTemplateBuilder builder) {
    this.geneApiBase = geneApiBase.replaceAll("/+$", "");
    this.rest = builder.build();
  }

  public Optional<String> symbolToHgncId(String symbol) {

    String url = String.format("%s/gene-search?query=%s", geneApiBase, symbol);
    log.debug("► GET {}", url);

    try {
      ResponseEntity<List<Map<String, Object>>> resp =
          rest.exchange(
              url,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<Map<String, Object>>>() {});

      List<Map<String, Object>> list = resp.getBody();

      if (resp.getStatusCode().is2xxSuccessful() && list != null && !list.isEmpty()) {

        String hgnc = (String) list.get(0).get("HGNC_ID");
        log.debug("{} → {}", symbol, hgnc);
        return Optional.ofNullable(hgnc);
      }
      log.warn("No match for {}", symbol);
      return Optional.empty();

    } catch (Exception ex) {
      log.warn("{} : {}", symbol, ex.getMessage());
      return Optional.empty();
    }
  }

  public String buildGeneUrl(String hgncId) {
    return String.format("%s/gene/%s", geneApiBase, hgncId);
  }
}

package uk.ac.ebi.subs.ingest.dataset.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GlobusService {
  private final GlobusProps props;

  private WebClient client(String base) {
    return WebClient.builder().baseUrl(base).build();
  }

  @SuppressWarnings("unchecked")
  public java.util.List<GlobusEntry> listEntries(String absPath) throws Exception {
    Map<String, Object> res = ls(absPath);
    Object data = res.get("DATA");
    if (!(data instanceof java.util.List<?>)) {
      throw new RuntimeException("Unexpected ls response DATA: " + data);
    }

    java.util.List<?> rawList = (java.util.List<?>) data;
    java.util.List<GlobusEntry> entries = new java.util.ArrayList<>();

    for (Object o : rawList) {
      if (!(o instanceof Map)) continue;
      Map<String, Object> m = (Map<String, Object>) o;
      String name = String.valueOf(m.get("name"));
      String type = String.valueOf(m.get("type"));
      Long size = null;
      Object sz = m.get("size");
      if (sz instanceof Number) {
        size = ((Number) sz).longValue();
      }
      String path = absPath.endsWith("/") ? absPath + name : absPath + "/" + name;
      entries.add(new GlobusEntry(name, type, size, path));
    }
    return entries;
  }

  public boolean directoryExists(String absPath) {
    try {
      listEntries(absPath);
      return true;
    } catch (Exception e) {
      System.err.println("[Globus] directoryExists(" + absPath + ") -> false: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  public boolean pathExists(String baseDir, String rel) {
    String parent = baseDir;
    String name = rel;

    if (rel.contains("/")) {
      int idx = rel.lastIndexOf('/');
      parent = baseDir + "/" + rel.substring(0, idx);
      name = rel.substring(idx + 1);
    }

    try {
      Map<String, Object> listing = ls(parent);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> items = (List<Map<String, Object>>) listing.get("DATA");

      for (Map<String, Object> item : items) {
        if (name.equals(item.get("name"))) {
          return true;
        }
      }
      return false;
    } catch (Exception e) {
      return false;
    }
  }

  // simple POJO for listEntries
  @lombok.Data
  @lombok.AllArgsConstructor
  public static class GlobusEntry {
    private String name;
    private String type; // "file" or "dir"
    private Long size;
    private String path;
  }

  public String remoteDatasetRoot(String datasetId) {
    String base = props.getBasePath(); // /ebi/ftp/private/morphic-transfer
    if (base == null || base.isBlank()) {
      throw new IllegalStateException("morphic.globus.base-path is not configured");
    }
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }

    String uploadSubdir = System.getenv("MORPHIC_UPLOAD_SUBDIR"); // "submissions"
    if (uploadSubdir != null && !uploadSubdir.isBlank()) {
      uploadSubdir = uploadSubdir.replaceAll("^/+", "").replaceAll("/+$", "");
      base = base + "/" + uploadSubdir;
    }

    String root = base + "/" + datasetId;
    System.out.println("[Globus] remoteDatasetRoot(" + datasetId + ") = " + root);
    return root;
  }

  public void mkdir(String absPath) {
    String tok = tokenForTransfer();
    System.out.printf("[Globus] mkdir endpoint=%s path=%s%n",
            props.getCollectionId(), absPath);

    var body = Map.of("DATA_TYPE", "mkdir", "path", absPath);

    client(props.getTransferUrl())
            .post()
            .uri("/operation/endpoint/{id}/mkdir", props.getCollectionId())
            .header("Authorization", "Bearer " + tok)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(body), Map.class)
            .exchange()
            .flatMap(response -> {
              if (response.statusCode().is2xxSuccessful()) {
                return Mono.empty();
              }
              if (response.statusCode().equals(HttpStatus.CONFLICT)) {
                System.out.printf(
                        "[Globus] mkdir: directory %s already exists (409); treating as success%n",
                        absPath);
                return Mono.empty();
              }
              return response.bodyToMono(String.class)
                      .flatMap(err -> Mono.error(new RuntimeException(
                              "Globus mkdir " + response.statusCode() + " body=" + err)));
            })
            .block();
  }

  public Map<String, Object> ls(String absPath) {
    String tok = tokenForTransfer();
    System.out.println("[Globus] ls endpoint=" + props.getCollectionId() + " path=" + absPath);

    return client(props.getTransferUrl())
        .get()
        .uri(
            uri ->
                uri.path("/operation/endpoint/{id}/ls")
                    .queryParam("path", absPath)
                    .build(props.getCollectionId()))
        .header("Authorization", "Bearer " + tok)
        .retrieve()
        .onStatus(
            HttpStatus::isError,
            r ->
                r.bodyToMono(String.class)
                    .flatMap(
                        b ->
                            Mono.error(
                                new RuntimeException(
                                    "[Globus] ls error " + r.statusCode() + " body=" + b))))
        .bodyToMono(Map.class)
        .block();
  }

  private String tokenForTransfer() {
    String collectionId = props.getCollectionId();

    String dataAccess =
        (props.getDataAccessScope() != null && !props.getDataAccessScope().isBlank())
            ? props.getDataAccessScope()
            : "https://auth.globus.org/scopes/" + collectionId + "/data_access";
    String scopes = dataAccess + " " + props.getTransferScope(); // keep both

    Map<String, Object> resp =
        client(props.getAuthUrl())
            .post()
            .uri("/v2/oauth2/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
            .body(
                BodyInserters.fromFormData("grant_type", "client_credentials")
                    .with("scope", scopes)
                    .with("client_id", props.getClientId())
                    .with("client_secret", props.getClientSecret()))
            .retrieve()
            .bodyToMono(Map.class)
            .block();

    java.util.List<Map<String, Object>> tokens = new java.util.ArrayList<>();
    tokens.add(resp);
    tokens.addAll(
        (java.util.List<Map<String, Object>>)
            resp.getOrDefault("other_tokens", java.util.List.of()));

    String transferToken =
        tokens.stream()
            .filter(t -> "transfer.api.globus.org".equals(String.valueOf(t.get("resource_server"))))
            .map(t -> String.valueOf(t.get("access_token")))
            .findFirst()
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "No token for resource_server=transfer.api.globus.org. Got resource_servers="
                            + tokens.stream()
                                .map(t -> String.valueOf(t.get("resource_server")))
                                .collect(java.util.stream.Collectors.toList())));

    return transferToken;
  }

  public void addAclRule(String path, String principalId, String permissions) {
    String tok = tokenForTransfer();

    // Use ACL collection if set, otherwise fall back to main collectionId
    String collectionId = (props.getAclCollectionId() != null && !props.getAclCollectionId().isBlank())
            ? props.getAclCollectionId()
            : props.getCollectionId();

    Map<String, Object> rule = new HashMap<>();
    rule.put("DATA_TYPE", "access");
    rule.put("principal_type", "identity");
    rule.put("principal", principalId);
    rule.put("path", path);
    rule.put("permissions", permissions);

    System.out.printf(
            "[Globus] Adding ACL rule on endpoint=%s path=%s principal=%s perms=%s%n",
            collectionId, path, principalId, permissions);

    client(props.getTransferUrl())
            .post()
            .uri("/endpoint/{id}/access", collectionId)
            .header("Authorization", "Bearer " + tok)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(rule), Map.class)
            .exchange()
            .flatMap(response -> {
              if (response.statusCode().is2xxSuccessful()) {
                return Mono.empty();
              }
              if (response.statusCode().equals(HttpStatus.CONFLICT)) {
                System.out.printf(
                        "[Globus] ACL rule already exists on %s for principal %s (409); treating as success%n",
                        path, principalId);
                return Mono.empty();
              }
              return response.bodyToMono(String.class)
                      .flatMap(err -> Mono.error(new RuntimeException(
                              "Globus ACL error " + response.statusCode() + " body=" + err)));
            })
            .block();
  }


  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> listAclRules() {
    String tok = tokenForTransfer();
    Map<String, Object> resp =
            client(props.getTransferUrl())
                    .get()
                    .uri("/endpoint/{id}/access_list", props.getCollectionId())
                    .header("Authorization", "Bearer " + tok)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

    Object data = resp.get("DATA");
    if (data instanceof List<?>) {
      return (List<Map<String, Object>>) data;
    }
    return List.of();
  }

  public String getSubmissionId() {
    String tok = tokenForTransfer();

    Map<?, ?> resp =
        client(props.getTransferUrl())
            .get()
            .uri("/submission_id")
            .header("Authorization", "Bearer " + tok)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

    Object value = resp.get("value");
    if (value == null) throw new RuntimeException("Globus submission_id missing 'value'");

    // Corrected logging to use a placeholder or simply System.out.println(value)
    System.out.println("[Globus] Fetched new submission_id: " + value);
    return value.toString();
  }

  public Map<String, Object> submitDelete(
      String baseDir, List<String> relPaths, boolean recursive) {
    String tok = tokenForTransfer();
    String submissionId = getSubmissionId();

    List<Map<String, Object>> items = new ArrayList<>();
    for (String rel : relPaths) {
      if (rel == null || rel.isEmpty()) continue;

      String path = baseDir.endsWith("/") ? baseDir + rel : baseDir + "/" + rel;

      Map<String, Object> item = new HashMap<>();
      item.put("DATA_TYPE", "delete_item");
      item.put("path", path);
      items.add(item);
    }

    if (items.isEmpty()) {
      throw new IllegalArgumentException("No valid paths given for delete");
    }

    Map<String, Object> body = new HashMap<>();
    body.put("DATA_TYPE", "delete");
    body.put("submission_id", submissionId);
    body.put("endpoint", props.getCollectionId());
    body.put("recursive", recursive);
    body.put("ignore_missing", Boolean.TRUE);
    body.put("DATA", items);

    System.out.printf("[Globus] Submitting delete for %d paths under %s%n", items.size(), baseDir);

    return client(props.getTransferUrl())
        .post()
        .uri("/delete")
        .header("Authorization", "Bearer " + tok)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Mono.just(body), Map.class) // <-- FIXED HERE
        .retrieve()
        .bodyToMono(Map.class)
        .block();
  }
}

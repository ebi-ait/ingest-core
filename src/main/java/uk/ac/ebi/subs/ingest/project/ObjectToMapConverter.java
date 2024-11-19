package uk.ac.ebi.subs.ingest.project;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class ObjectToMapConverter<T> {

  private ObjectMapper objectMapper;

  public <T> Map<String, Object> asMap(T target, List<String> excludeList) {
    if (objectMapper == null) {
      objectMapper = new ObjectMapper();
    }
    Map<String, Object> projectAsMap = objectMapper.convertValue(target, new TypeReference<>() {});
    excludeList.forEach(projectAsMap::remove);
    return projectAsMap;
  }

  public <T> Map<String, Object> asMap(T target) {
    return asMap(target, List.of());
  }
}

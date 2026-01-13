package uk.ac.ebi.subs.ingest.dataset.util;

import java.util.*;

public final class ContentMaps {
  private ContentMaps() {}

  @SuppressWarnings("unchecked")
  public static Map<String, Object> asMap(Object content) {
    if (content == null) return new LinkedHashMap<>();
    if (content instanceof Map) return new LinkedHashMap<>((Map<String, Object>) content);
    // If content is something else, we don't want to break old data; wrap it.
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("rawContent", content);
    return m;
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> getOrCreateMap(Map<String, Object> parent, String key) {
    Object v = parent.get(key);
    if (v instanceof Map) return (Map<String, Object>) v;
    Map<String, Object> child = new LinkedHashMap<>();
    parent.put(key, child);
    return child;
  }

  @SuppressWarnings("unchecked")
  public static List<Object> getOrCreateList(Map<String, Object> parent, String key) {
    Object v = parent.get(key);
    if (v instanceof List) return (List<Object>) v;
    List<Object> list = new ArrayList<>();
    parent.put(key, list);
    return list;
  }
}

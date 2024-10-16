package uk.ac.ebi.subs.ingest.schemas.schemascraper.impl;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/** Created by rolando on 19/04/2018. */
public class ListBucketResult {
  public ListBucketResult() {}

  @JacksonXmlProperty(localName = "Name")
  public String name;

  @JacksonXmlProperty(localName = "Contents")
  public List<Content> contents = new ArrayList<>();

  static class Content {
    Content() {}

    @JacksonXmlProperty(localName = "Key")
    public String key;

    public void setKey(String key) {
      this.key = key;
    }

    public String getKey() {
      return this.key;
    }
  }
}

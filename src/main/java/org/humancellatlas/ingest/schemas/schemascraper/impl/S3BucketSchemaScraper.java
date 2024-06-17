package org.humancellatlas.ingest.schemas.schemascraper.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.stream.Collectors;

import org.humancellatlas.ingest.schemas.schemascraper.SchemaScraper;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.NonNull;

/**
 * Created by rolando on 19/04/2018.
 *
 * <p>Scrapes schemas from an s3 bucket's default XML file-list page
 */
@Service
public class S3BucketSchemaScraper implements SchemaScraper {
  private final @NonNull RestTemplate restTemplate;
  private final @NonNull XmlMapper xmlMapper;

  public S3BucketSchemaScraper() {
    this.restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

    JacksonXmlModule xmlModule = new JacksonXmlModule();
    xmlModule.setDefaultUseWrapper(false);
    XmlMapper xmlMapper = new XmlMapper(xmlModule);
    xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    this.xmlMapper = xmlMapper;
  }

  @Override
  public Collection<URI> getAllSchemaURIs(URI schemaBucketLocation) {
    String bucketListingXmlString =
        this.restTemplate.getForObject(schemaBucketLocation, String.class);
    try {
      ListBucketResult listBucketResult =
          xmlMapper.readValue(bucketListingXmlString, ListBucketResult.class);
      return listBucketResult.contents.stream()
          .map(content -> URI.create(content.getKey()))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new SchemaScrapeException(
          "Failed to parse schema bucket xml at URL: " + schemaBucketLocation, e);
    }
  }
}

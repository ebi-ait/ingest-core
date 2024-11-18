package org.humancellatlas.ingest.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {
      MetadataDocument.class,
    })
public class MetadataDocumentTest {
  @Test
  @DisplayName("Is Equal with matching Static Members")
  public void testSameStatics() {
    // given
    var map1 = Map.of("Key1", "Value1", "Key2", "Value2");
    var map2 = Map.of("Key1", "Value1", "Key2", "Value2");

    MetadataDocument doc1 = new DocumentTest(EntityType.PROJECT, "Identifier", map1);
    doc1.setUuid(Uuid.newUuid());
    MetadataDocument doc2 = new DocumentTest(EntityType.PROJECT, "Identifier", map2);
    doc2.setUuid(doc1.getUuid());

    assertThat(doc1).isEqualTo(doc2);
  }

  @Test
  @DisplayName("Is Equal with similar content")
  public void testSimilarContent() {
    // given
    var map1 = Map.of("Key1", "Value1", "Key2", "Value2");
    var map2 = Map.of("Key2", "Value2", "Key1", "Value1");
    MetadataDocument doc1 = new DocumentTest(EntityType.PROJECT, "Identifier", map1);
    MetadataDocument doc2 = new DocumentTest(EntityType.PROJECT, "Identifier", map2);

    assertThat(doc1).isEqualTo(doc2);
  }

  @Test
  @DisplayName("Is Not Equal with different id")
  public void testDifferentID() {
    // given
    var map1 = Map.of("Key1", "Value1", "Key2", "Value2");
    var map2 = Map.of("Key1", "Value1", "Key2", "Value2");
    MetadataDocument doc1 = new DocumentTest(EntityType.PROJECT, "Identifier-One", map1);
    MetadataDocument doc2 = new DocumentTest(EntityType.PROJECT, "Identifier-Two", map2);

    assertThat(doc1).isNotEqualTo(doc2);
  }

  @Test
  @DisplayName("Is Not Equal with different content")
  public void testDifferentContent() {
    // given
    var map1 = Map.of("Key1", "Value1", "Key2", "Value2");
    var map2 = Map.of("Key1", "Value1", "Key3", "Value3");
    MetadataDocument doc1 = new DocumentTest(EntityType.PROJECT, "Identifier", map1);
    MetadataDocument doc2 = new DocumentTest(EntityType.PROJECT, "Identifier", map2);

    assertThat(doc1).isNotEqualTo(doc2);
  }

  @Test
  @DisplayName("Is Not Equal with different content")
  public void testDifferentUUIDs() {
    // given
    var map1 = Map.of("Key1", "Value1", "Key2", "Value2");
    var map2 = Map.of("Key1", "Value1", "Key2", "Value2");
    MetadataDocument doc1 = new DocumentTest(EntityType.PROJECT, "Identifier", map1);
    doc1.setUuid(Uuid.newUuid());
    MetadataDocument doc2 = new DocumentTest(EntityType.PROJECT, "Identifier", map2);
    doc2.setUuid(Uuid.newUuid());

    assertThat(doc1).isNotEqualTo(doc2);
  }

  @Getter
  @EqualsAndHashCode(callSuper = true)
  static class DocumentTest extends MetadataDocument {
    DocumentTest(EntityType type, String id, Object content) {
      super(type, content);
      this.id = id;
    }
  }

  @Test
  @DisplayName("Is Equal with matching accessions")
  public void testAccessionsEquality() {
    // given
    var accessions1 = List.of("ENA:12345", "ENA:67890");
    var accessions2 = List.of("ENA:12345", "ENA:67890");

    MetadataDocument doc1 = new DocumentTest(EntityType.PROJECT, "Identifier", Map.of());
    doc1.setAccessions(accessions1);
    MetadataDocument doc2 = new DocumentTest(EntityType.PROJECT, "Identifier", Map.of());
    doc2.setAccessions(accessions2);

    // then
    assertThat(doc1).isEqualTo(doc2);
  }
}

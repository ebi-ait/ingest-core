package org.humancellatlas.ingest.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    MetadataDocument.class,
})
public class MetadataDocumentTest {
    @Test
    @DisplayName("Is Not Equal with same UUID and different content")
    public void testSameUUID(){
        //given
        MetadataDocument doc1 = new DocumentTest(EntityType.PROJECT, "Identifier1", "Content1");
        MetadataDocument doc2 = new DocumentTest(EntityType.PROJECT, "Identifier2", "Content2");
        doc2.setIsUpdate(true);
        doc2.setUuid(doc1.getUuid());
        assertThat(doc1).isNotEqualTo(doc2);
    }

    @Test
    @DisplayName("Is Equal with matching Static Members")
    public void testSameStatics(){
        //given
        MetadataDocument doc1 = new DocumentTest(EntityType.PROJECT, "Identifier1", "Content1");
        MetadataDocument doc2 = new DocumentTest(EntityType.PROJECT, "Identifier1", "Content1");

        assertThat(doc1).isEqualTo(doc2);
    }

    @Test
    @DisplayName("Is Not Equal with same Id and different content")
    public void testSameId(){
        //given
        MetadataDocument doc1 = new DocumentTest(EntityType.PROJECT, "Identifier1", "Content1");
        MetadataDocument doc2 = new DocumentTest(EntityType.PROJECT, "Identifier1", "Content2");

        assertThat(doc1).isNotEqualTo(doc2);
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    static
    class DocumentTest extends MetadataDocument{
        DocumentTest(EntityType type, String id, Object content) {
            super(type, content);
            this.id = id;
        }
    }
}

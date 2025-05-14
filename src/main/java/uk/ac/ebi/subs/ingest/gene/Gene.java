package uk.ac.ebi.subs.ingest.gene;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Document(collection = "genes")
public class Gene {

    @Id
    private String id;

    @Field("HGNC_ID")
    private String hgncId;

    @Field("Name")
    private String name;

    @Field("Full_Name")
    private String fullName;

    @Field("Gene_Group")
    private String geneGroup;

    @Field("Protein_Class")
    private String proteinClass;
}


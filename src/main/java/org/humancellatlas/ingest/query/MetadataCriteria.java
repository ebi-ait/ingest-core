package org.humancellatlas.ingest.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MetadataCriteria {
    String contentField;
    Operator operator;
    Object value;
}

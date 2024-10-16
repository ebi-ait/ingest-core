package uk.ac.ebi.subs.ingest.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class QueryBuilderTest {
  @Test
  public void testBuildOr() {
    // given:
    QueryBuilder queryBuilder = new QueryBuilder();

    List<MetadataCriteria> metadataCriteriaList = new ArrayList<>();
    metadataCriteriaList.add(new MetadataCriteria("field", Operator.IS, "value"));
    Query expectedQuery = new Query();
    Criteria criteria = Criteria.where("field").is("value");
    expectedQuery.addCriteria(new Criteria().orOperator(new Criteria[] {criteria}));

    // when:
    Query actualQuery = queryBuilder.build(metadataCriteriaList, false);

    // then:
    assertThat(actualQuery).isEqualTo(expectedQuery);
  }

  @Test
  public void testBuildAnd() {
    // given:
    QueryBuilder queryBuilder = new QueryBuilder();

    List<MetadataCriteria> metadataCriteriaList = new ArrayList<>();
    metadataCriteriaList.add(new MetadataCriteria("field", Operator.IS, "value"));
    Query expectedQuery = new Query();
    Criteria criteria = Criteria.where("field").is("value");
    expectedQuery.addCriteria(new Criteria().andOperator(new Criteria[] {criteria}));

    // when:
    Query actualQuery = queryBuilder.build(metadataCriteriaList, true);

    // then:
    assertThat(actualQuery).isEqualTo(expectedQuery);
  }
}

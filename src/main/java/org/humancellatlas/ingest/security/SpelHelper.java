package org.humancellatlas.ingest.security;

import java.util.List;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpelHelper {
  public SpelHelper() {}

  Boolean parseExpression(List<String> params, List<Object> args, String expression) {
    return parseExpression((String[]) params.toArray(new String[0]), args.toArray(), expression);
  }

  Boolean parseExpression(String[] params, Object[] args, String expression) {
    ExpressionParser parser = new SpelExpressionParser();
    StandardEvaluationContext context = new StandardEvaluationContext();

    for (int i = 0; i < params.length; i++) {
      context.setVariable(params[i], args[i]);
    }

    return parser.parseExpression(expression).getValue(context, Boolean.class);
  }
}

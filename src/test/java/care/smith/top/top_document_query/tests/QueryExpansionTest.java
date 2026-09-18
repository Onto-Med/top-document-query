package care.smith.top.top_document_query.tests;

import static org.junit.jupiter.api.Assertions.*;

import care.smith.top.model.Expression;
import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;
import care.smith.top.top_document_query.functions.And;
import care.smith.top.top_document_query.functions.Dist;
import care.smith.top.top_document_query.functions.Not;
import care.smith.top.top_document_query.functions.Or;
import care.smith.top.top_document_query.functions.XProd;
import care.smith.top.top_document_query.query_expansion.QueryExpansionExpressionCompiler;
import care.smith.top.top_document_query.query_expansion.QueryExpansionStrategy;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class QueryExpansionTest {

  @Test
  public void parsesQueryExpansionAdapterConfig() {
    String configPath = getClass().getResource("/config/Example_Adapter_Local.yml").getPath();
    TextAdapterConfig config = TextAdapterConfig.getInstance(configPath);

    assertNotNull(config.getQueryExpansion());
    assertEquals("medical_de", config.getQueryExpansion().getProfile());
    assertEquals(
        "alternatives",
        config.getQueryExpansion().getRelations().get("equivalent_to").getStrategy());
    assertEquals(
        "require_context",
        config.getQueryExpansion().getRelations().get("treated_by").getStrategy());
    assertEquals(
        "proximity_context",
        config.getQueryExpansion().getRelations().get("investigated_by").getStrategy());
    assertEquals(
        "phrase_context",
        config.getQueryExpansion().getRelations().get("confirmed_by").getStrategy());
  }

  @Test
  public void exposesQueryExpansionStrategyDefinitions() {
    assertEquals(
        QueryExpansionStrategy.values().length, QueryExpansionStrategy.getDefinitions().size());
    assertTrue(
        QueryExpansionStrategy.getDefinitions().stream()
            .anyMatch(
                definition ->
                    "proximity_context".equals(definition.getId())
                        && definition.isExpressionSupported()));
  }

  @Test
  public void compilesQueryExpansionStrategies() {
    assertCompiledFunction("alternatives", Or.ID);
    assertCompiledFunction("optional_context", Or.ID);
    assertCompiledFunction("recall_expansion", Or.ID);
    assertCompiledFunction("require_context", And.ID);
    assertCompiledFunction("specificity_focus", And.ID);

    Expression exclude =
        QueryExpansionExpressionCompiler.compileRelation("exclude_context", "source", "target")
            .orElseThrow();
    assertEquals(And.ID, exclude.getFunctionId());
    assertEquals(Not.ID, exclude.getArguments().get(1).getFunctionId());

    Expression proximity =
        QueryExpansionExpressionCompiler.compileRelation("proximity_context", "source", "target")
            .orElseThrow();
    assertEquals(Dist.ID, proximity.getFunctionId());
    assertEquals(XProd.ID, proximity.getArguments().get(0).getFunctionId());

    Expression phrase =
        QueryExpansionExpressionCompiler.compileRelation("phrase_context", "source", "target")
            .orElseThrow();
    assertEquals(XProd.ID, phrase.getFunctionId());

    Optional<Expression> ignored =
        QueryExpansionExpressionCompiler.compileRelation("ignore", "source", "target");
    assertTrue(ignored.isEmpty());
  }

  private void assertCompiledFunction(String strategy, String functionId) {
    Expression expression =
        QueryExpansionExpressionCompiler.compileRelation(strategy, "source", "target")
            .orElseThrow();
    assertEquals(functionId, expression.getFunctionId());
  }
}

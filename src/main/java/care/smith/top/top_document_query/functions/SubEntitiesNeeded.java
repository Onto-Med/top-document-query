package care.smith.top.top_document_query.functions;

import care.smith.top.model.Expression;

import java.util.List;

public interface SubEntitiesNeeded {
  int getDepth(List<Expression> args);
}

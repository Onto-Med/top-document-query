package care.smith.top.top_document_query.adapter;

public abstract class AbstractDocument {
  public String name;
  public String text;

  abstract String getName();
  abstract void setName(String name);
  abstract String getText();
  abstract void setText(String text);
}

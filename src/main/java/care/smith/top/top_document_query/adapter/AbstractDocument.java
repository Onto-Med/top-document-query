package care.smith.top.top_document_query.adapter;

public abstract class AbstractDocument {
  public String name;
  public String text;
  public String id;

  public abstract String getId();

  abstract void setId(String id);

  public abstract String getName();

  abstract void setName(String name);

  public abstract String getText();

  abstract void setText(String text);
}

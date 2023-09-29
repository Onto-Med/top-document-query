package care.smith.top.top_document_query.adapter;

public class ElasticDocument extends AbstractDocument {

  public String name;
  public String text;
  public String id;
  public double score;

  @Override
  public String getId() { return null; }

  @Override
  public void setId(String id) { this.id = id; }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public void setText(String text) {
    this.text = text;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score){
    this.score = score;
  }
}

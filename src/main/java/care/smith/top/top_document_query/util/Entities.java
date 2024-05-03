package care.smith.top.top_document_query.util;

import care.smith.top.model.*;
import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Entities {
  private final Map<String, Entity> entities = new LinkedHashMap<>();

  private Repository repo;

  private Entities(Entity... entities) {
    add(entities);
  }

  private Entities(List<Entity> entities) {
    for (Entity e : entities) add(e);
  }

  public void add(Entity e) {
    entities.put(e.getId(), e);
  }

  public void add(Entity... entities) {
    for (Entity e : entities) add(e);
  }

  public Concept getConcept(String conceptId) {
    Entity entity = entities.get(conceptId);
    return entity instanceof Concept ? (Concept) entity : null;
  }

  public static Entities of(Entity... entities) {
    return new Entities(entities);
  }

  public static Entities of(List<Entity> entities) {
    return new Entities(entities);
  }

  public static Entities of(Repository repo, Entity... entities) {
    return new Entities(entities).repository(repo);
  }

  public static Entities of(Repository repo, List<Entity> entities) {
    return new Entities(entities).repository(repo);
  }

  public Entities repository(Repository repo) {
    this.repo = repo;
    return this;
  }

  public Repository getRepository() {
    return repo;
  }

  public String getRepoName() {
    return repo.getName();
  }

  public String getRepoDescription() {
    return repo.getDescription();
  }

  public Entity getEntity(String id) {
    return entities.get(id);
  }

  public Collection<Entity> getEntities() {
    return entities.values();
  }

  public Entity[] getEntitiesArray() {
    return getEntities().toArray(new Entity[0]);
  }

  public Collection<Concept> getConcepts() {
    return getEntities().stream()
        .filter(
            e ->
                Arrays.asList(EntityType.SINGLE_CONCEPT, EntityType.COMPOSITE_CONCEPT)
                    .contains(e.getEntityType()))
        .map(Concept.class::cast)
        .collect(Collectors.toSet());
  }

  public Set<String> getIds() {
    return entities.keySet();
  }

  public static String getFirstTitle(Entity e) {
    return e.getTitles().get(0).getText();
  }

  public static List<String> getTitles(Entity e) {
    return getAnnotations(e.getTitles());
  }

  public static List<String> getSynonyms(Entity e) {
    return getAnnotations(e.getSynonyms());
  }

  public static List<String> getDescriptions(Entity e) {
    return getAnnotations(e.getDescriptions());
  }

  private static List<String> getAnnotations(List<LocalisableText> texts) {
    if (texts == null) return new ArrayList<>();
    return texts.stream().map(t -> toString(t)).collect(Collectors.toList());
  }

  private static String toString(LocalisableText txt) {
    return (txt.getLang() == null) ? txt.getText() : txt.getText() + PROP_VAL_SEP + txt.getLang();
  }

  public static String getTitle(Entity e, String lang) {
    return getText(e.getTitles(), lang);
  }

  public static String getSynonym(Entity e, String lang) {
    return getText(e.getSynonyms(), lang);
  }

  public static String getDescription(Entity e, String lang) {
    return getText(e.getDescriptions(), lang);
  }

  private static String getText(List<LocalisableText> texts, String lang) {
    if (lang == null) {
      return (texts.isEmpty()) ? null : texts.get(0).getText();
    }
    return texts.stream()
        .filter(t -> Objects.equals(t.getLang(), lang))
        .map(LocalisableText::getText)
        .findFirst()
        .orElse(null);
  }

  private static final String ANN_SEP = "::";
  private static final String PROP_VAL_SEP = "|";

  public static Set<String> getTitlesAndSynonyms(Entity e, String lang) {
    Set<String> terms = new LinkedHashSet<>();
    terms.addAll(getAnnotations(e.getTitles(), lang));
    if (e.getSynonyms() != null && !e.getSynonyms().isEmpty())
      terms.addAll(getAnnotations(e.getSynonyms(), lang));
    return terms;
  }

  public static Set<String> getCodeTitle(Entity e, String lang) {
    Set<String> codeRepr = new LinkedHashSet<>();
    if (e.getCodes() != null && !e.getCodes().isEmpty()) {
      for (Code c : e.getCodes()) {
        codeRepr.add(c.getName());
        codeRepr.addAll(c.getSynonyms());
      }
    }
    return codeRepr;
  }

  public static Set<String> getTerms(Entity e, String lang, boolean includeSubTree) {
    Set<String> terms = getTitlesAndSynonyms(e, lang);
    terms.addAll(getCodeTitle(e, lang));
    if (!includeSubTree) return terms;
    if (!(e instanceof SingleConcept)) return terms;

    SingleConcept c = (SingleConcept) e;
    if (c.getSubConcepts() != null) {
      for (Concept child : c.getSubConcepts()) {
        terms.addAll(getTerms(child, lang, includeSubTree));
      }
    }

    return terms;
  }

  private static List<String> getAnnotations(List<LocalisableText> txts, String lang) {
    Stream<LocalisableText> textStream = txts.stream();
    //      // only filter annotations by language if lang is set
    if (lang != null)
      textStream =
          textStream.filter(t -> lang.trim().equals(t.getLang().trim()) && !t.getText().isBlank());
    // else take all that are available
    return textStream.map(t -> t.getText().trim()).collect(Collectors.toList());
  }

  public static String getAnnotations(Entity e) {
    String txt = toString("title", e.getTitles());
    if (e.getSynonyms() != null && !e.getSynonyms().isEmpty())
      txt += ANN_SEP + toString("synonym", e.getSynonyms());
    if (e.getDescriptions() != null && !e.getDescriptions().isEmpty())
      txt += ANN_SEP + toString("description", e.getDescriptions());
    return txt;
  }

  private static String toString(String prop, List<LocalisableText> txts) {
    return txts.stream()
        .map(t -> prop + PROP_VAL_SEP + toString(t))
        .collect(Collectors.joining(ANN_SEP));
  }

  public static void addAnnotations(Entity e, String props) {
    if (props == null || props.isBlank()) return;
    String[] anns = props.split("\\s*" + ANN_SEP + "\\s*");
    for (String ann : anns) add(e, ann.split("\\s*\\" + PROP_VAL_SEP + "\\s*"));
  }

  private static void add(Entity e, String[] vals) {
    LocalisableText txt = new LocalisableText();

    if (vals.length == 1) {
      e.addTitlesItem(txt.text(vals[0]));
      return;
    }

    txt.text(vals[1]);
    if (vals.length > 2) txt.lang(vals[2]);

    if ("title".equalsIgnoreCase(vals[0])) e.addTitlesItem(txt);
    else if ("synonym".equalsIgnoreCase(vals[0])) e.addSynonymsItem(txt);
    else if ("description".equalsIgnoreCase(vals[0])) e.addDescriptionsItem(txt);
  }

  public int size() {
    return entities.size();
  }

  @Override
  public String toString() {
    return "Entities [entities=" + entities.values() + "]";
  }
}

package care.smith.top.top_phenotypic_query.util;

import care.smith.top.model.*;
import care.smith.top.top_phenotypic_query.data_adapter.config.CodeMapping;
import care.smith.top.top_phenotypic_query.data_adapter.config.DataAdapterConfig;
import care.smith.top.top_phenotypic_query.util.builder.Exp;
import care.smith.top.top_phenotypic_query.util.builder.Res;
import java.io.IOException;
import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

  public Entities deriveAdditionalProperties(DataAdapterConfig config) {
    for (Phenotype p : getPhenotypes()) {
      Phenotype supP = p.getSuperPhenotype();
      if (supP != null) {
        supP = getPhenotype(supP.getId());
        p.setSuperPhenotype(supP);
      }
      if (Phenotypes.isRestriction(p)) {
        if (Phenotypes.isSingle(p) && p.getRestriction() == null) p.setRestriction(Res.ofCodes(p));
        if (config == null
            || Phenotypes.isCompositePhenotype(supP)
            || !setInExpression(config, supP, p)) p.setExpression(Exp.inRestriction(p));
      }
    }
    return this;
  }

  public Entities deriveAdditionalProperties() {
    return deriveAdditionalProperties(null);
  }

  public Concept getConcept(String conceptId) {
    Entity entity = entities.get(conceptId);
    return entity instanceof Concept ? (Concept) entity : null;
  }

  private boolean setInExpression(DataAdapterConfig config, Phenotype supP, Phenotype p) {
    CodeMapping codeMap = config.getCodeMappingIncludingSubjectParameters(supP);
    if (codeMap == null) return false;
    Restriction sourceRestr = codeMap.getSourceRestriction(p.getRestriction(), supP);
    p.setExpression(Exp.inRestriction(supP, sourceRestr));
    return true;
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

  public static Entities of(String repoUrl, String user, String password)
      throws IOException, InterruptedException {
    String token = HTTP.getToken(user, password);
    return of(HTTP.readRepository(repoUrl, token), HTTP.readEntities(repoUrl, token));
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

  public Phenotype getPhenotype(String id) {
    return (Phenotype) getEntity(id);
  }

  public Category getCategory(String id) {
    return (Category) getEntity(id);
  }

  public Phenotype getPhenotypeWithTitle(String title) {
    for (Phenotype p : getPhenotypes())
      for (LocalisableText t : p.getTitles()) if (title.equalsIgnoreCase(t.getText())) return p;
    return null;
  }

  public Collection<Entity> getEntities() {
    return entities.values();
  }

  public Entity[] getEntitiesArray() {
    return getEntities().toArray(new Entity[0]);
  }

  public Collection<Category> getCategories() {
    return getEntities().stream()
        .filter(e -> e.getEntityType() == EntityType.CATEGORY)
        .map(Category.class::cast)
        .collect(Collectors.toSet());
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

  public Set<String> getPhenotypeIds() {
    return getPhenotypes().stream().map(e -> e.getId()).collect(Collectors.toSet());
  }

  public Collection<Phenotype> getPhenotypes() {
    return getEntities().stream()
        .filter(e -> e.getEntityType() != EntityType.CATEGORY)
        .map(Phenotype.class::cast)
        .collect(Collectors.toList());
  }

  public Collection<Phenotype> getPhenotypes(EntityType type) {
    return getEntities().stream()
        .filter(e -> e.getEntityType() == type)
        .map(Phenotype.class::cast)
        .collect(Collectors.toList());
  }

  public Collection<Phenotype> getSinglePhenotypes() {
    return getPhenotypes(EntityType.SINGLE_PHENOTYPE);
  }

  public Collection<Phenotype> getSingleRestrictions() {
    return getPhenotypes(EntityType.SINGLE_RESTRICTION);
  }

  public Collection<Phenotype> getCompositePhenotypes() {
    return getPhenotypes(EntityType.COMPOSITE_PHENOTYPE);
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
    return texts.stream()
        .filter(t -> Objects.equals(t.getLang(), lang))
        .map(t -> t.getText())
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

  public static Set<String> getTerms(Entity e, String lang, boolean includeSubTree) {
    Set<String> terms = getTitlesAndSynonyms(e, lang);
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
    return txts.stream()
        .filter(t -> lang.trim().equals(t.getLang().trim()) && !t.getText().isBlank())
        .map(t -> t.getText().trim())
        .collect(Collectors.toList());
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

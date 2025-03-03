package care.smith.top.top_document_query.adapter;

import care.smith.top.model.*;
import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;
import care.smith.top.top_document_query.util.Entities;
import care.smith.top.top_document_query.util.TermConcatenationTypes;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;

public abstract class TextAdapter {

  protected TextAdapterConfig config;

  protected TextAdapter(TextAdapterConfig config) {
    this.config = config;
  }

  protected TextAdapter(String configFile) {
    this(TextAdapterConfig.getInstance(configFile));
  }

  public static TextAdapter getInstance(TextAdapterConfig config) throws InstantiationException {
    TextAdapter adapter;
    try {
      Class<?> adapterClass = Class.forName(config.getAdapter());
      adapter =
          (TextAdapter) adapterClass.getConstructor(TextAdapterConfig.class).newInstance(config);
    } catch (ClassNotFoundException
        | InvocationTargetException
        | IllegalAccessException
        | NoSuchMethodException
        | ClassCastException e) {
      e.printStackTrace();
      throw new InstantiationException("Could not instantiate adapter for provided configuration.");
    }
    return adapter;
  }

  public static TextAdapter getInstance(String configFile) throws InstantiationException {
    return getInstance(TextAdapterConfig.getInstance(configFile));
  }

  /**
   * Builds the flattened hierarchy back to model sub dependencies of Concepts.
   *
   * @param entities A {@link Map} that features all potential concept instances of a query.
   * @param dependencies A {@link Map} holds all dependencies as a {@link Set} of ids of a concept
   *     (as id).
   * @return An instance of {@link Entities}.
   */
  protected Entities buildConceptHierarchy(
      Map<String, Entity> entities, Map<String, Set<String>> dependencies) {
    for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
      Entity eEntity = entities.get(entry.getKey());
      if (eEntity instanceof SingleConcept) {
        ((SingleConcept) eEntity)
            .setSubConcepts(
                entry.getValue().stream()
                    .map(e -> ((SingleConcept) entities.get(e)))
                    .collect(Collectors.toList()));
      }
    }
    return Entities.of(entities.values().toArray(new Entity[0]));
  }

  public abstract Map<String, Integer> getSubconceptDepths(ConceptQuery query, Entities concepts);

  public abstract Stream<List<DocumentHit>> execute(
      ConceptQuery query, Map<String, Entity> entities, Map<String, Set<String>> dependencies);

  public abstract Stream<List<DocumentHit>> execute(String queryString);

  public abstract long count();

  public TextAdapterConfig getConfig() {
    return config;
  }

  public abstract Stream<List<Document>> getAllDocumentsBatched(
      Integer batchSize, Boolean simplified);

  public abstract Page<Document> getAllDocumentsPaged(Integer page, Boolean simplified)
      throws IOException;

  public abstract Page<Document> getDocumentsByNamePaged(
      @NonNull String documentName, Integer page, Boolean simplified) throws IOException;

  public abstract Stream<List<Document>> getDocumentsByNameBatched(
      @NonNull String documentName, Integer batchSize, Boolean simplified) throws IOException;

  public abstract Optional<Document> getDocumentById(@NonNull String documentId, Boolean simplified)
      throws IOException;

  public abstract Page<Document> getDocumentsByIdsPaged(
      @NonNull Collection<String> ids, Integer page, Boolean simplified) throws IOException;

  public abstract Stream<List<Document>> getDocumentsByIdsBatched(
      @NonNull Collection<String> ids, Integer batchSize, Boolean simplified) throws IOException;

  public abstract Page<Document> getDocumentsByTerms(
      @NonNull Collection<String> terms, Integer page, Boolean simplified) throws IOException;

  public abstract Page<Document> getDocumentsByTerms(
      @NonNull Collection<String> terms,
      TermConcatenationTypes concatenationTypes,
      Integer page,
      Boolean simplified)
      throws IOException;

  public abstract Page<Document> getDocumentsByIdsAndTerms(
      @NonNull Collection<String> ids,
      @NonNull Collection<String> terms,
      Integer page,
      Boolean simplified)
      throws IOException;

  public abstract Page<Document> getDocumentsByIdsAndTerms(
      @NonNull Collection<String> ids,
      @NonNull Collection<String> terms,
      TermConcatenationTypes concatenationTypes,
      Integer page,
      Boolean simplified)
      throws IOException;
}

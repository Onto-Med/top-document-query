package care.smith.top.top_document_query.adapter;

import care.smith.top.model.ConceptQuery;
import care.smith.top.model.Document;
import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;
import care.smith.top.top_document_query.util.Entities;
import care.smith.top.top_document_query.util.TermConcatenationTypes;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

  public abstract List<DocumentHit> execute(ConceptQuery query, Entities entities);

  public abstract List<DocumentHit> execute(String queryString);

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

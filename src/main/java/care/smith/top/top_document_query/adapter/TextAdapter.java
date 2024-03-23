package care.smith.top.top_document_query.adapter;

import care.smith.top.model.ConceptQuery;
import care.smith.top.model.Document;
import care.smith.top.top_document_query.adapter.config.TextAdapterConfig;
import care.smith.top.top_document_query.util.Entities;
import care.smith.top.top_document_query.util.TermConcatenationTypes;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

  public abstract Stream<List<Document>> getAllDocumentsBatched(Integer batchSize);

  public abstract Page<Document> getAllDocuments(Integer page) throws IOException;

  public abstract Optional<Document> getDocumentById(@NonNull String documentId) throws IOException;

  public abstract Page<Document> getDocumentsByName(@NonNull String documentName, Integer page)
          throws IOException;

  public abstract Page<Document> getDocumentsByIds(@NonNull Collection<String> ids, Integer page)
          throws IOException;

  public abstract Page<Document> getDocumentsByTerms(@NonNull Collection<String> phrases, Integer page)
          throws IOException;

  public abstract Page<Document> getDocumentsByTerms(@NonNull Collection<String> phrases, TermConcatenationTypes concatenationTypes, Integer page)
          throws IOException;

  public abstract Page<Document> getDocumentsByIdsAndPhrases(
          @NonNull Collection<String> ids, @NonNull Collection<String> phrases, Integer page);

//  public abstract Page<Document> getDocumentsByTerms(String[] terms, String[] fields);
//
//  public abstract Page<Document> getDocumentsByTermsBoolean(
//          String[] mustTerms, String[] shouldTerms, String[] notTerms, String[] fields);
//
//  public abstract Page<Document> getDocumentsByPhrases(String[] phrases, String[] fields);
//
//  public abstract Page<Document> getDocumentsByPhrasesBoolean(
//          String[] mustPhrases, String[] shouldPhrases, String[] notPhrases, String[] fields);
}

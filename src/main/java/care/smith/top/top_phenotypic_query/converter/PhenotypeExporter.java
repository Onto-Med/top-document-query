package care.smith.top.top_phenotypic_query.converter;

import care.smith.top.model.Entity;
import care.smith.top.model.Repository;
import java.io.File;
import java.io.OutputStream;

/**
 * Implementations of this interface are used to convert a list of phenotypes to a specific format.
 * One can provide a {@link File} or {@link OutputStream} where the result will be written to.
 */
public interface PhenotypeExporter {

  /**
   * Get the file extension an output file is expected to have.
   *
   * @return The file extension.
   */
  String getFileExtension();

  /**
   * Convert a list of phenotypes to a specific format and write the result to an {@link
   * OutputStream}.
   *
   * @param entities The list of phenotypes.
   * @param repo A repository object that may be used to set additional metadata, depending on the
   *     format being converted to.
   * @param uri A URI that may be stored as additional metadata, depending on the format being
   *     converted to.
   * @param stream The stream the result will be written to.
   */
  void write(Entity[] entities, Repository repo, String uri, OutputStream stream);

  /**
   * Convert a list of phenotypes to a specific format and write the result to a {@link File}.
   *
   * @param entities The list of phenotypes.
   * @param repo A repository object that may be used to set additional metadata, depending on the
   *     format being converted to.
   * @param uri A URI that may be stored as additional metadata, depending on the format being
   *     converted to.
   * @param file The file the result will be written to.
   */
  void write(Entity[] entities, Repository repo, String uri, File file);
}

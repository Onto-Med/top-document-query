package care.smith.top.top_phenotypic_query.converter;

import care.smith.top.model.Entity;
import java.io.File;
import java.io.InputStream;

/**
 * Implementations of this interface are used to convert a specifically formatted input into a list
 * of phenotypes.
 */
public interface PhenotypeImporter {

  /**
   * Get the file extension an input file is expected to have.
   *
   * @return The file extension.
   */
  String getFileExtension();

  /**
   * Read from an input stream and convert it's content to a list of phenotypes.
   *
   * @param stream The input stream
   * @return The resulting phenotype list.
   */
  Entity[] read(InputStream stream);

  /**
   * Read from a file and convert it's content to a list of phenotypes.
   *
   * @param file The input file.
   * @return The resulting phenotype list.
   */
  Entity[] read(File file);
}

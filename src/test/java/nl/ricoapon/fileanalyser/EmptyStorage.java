package nl.ricoapon.fileanalyser;

/**
 * Class that needs to be public for the test in {@link nl.ricoapon.fileanalyser.FileAnalyserTest}, because it will
 * be instantiated using reflection. This fails if the class is protected or private.
 */
public class EmptyStorage {
}

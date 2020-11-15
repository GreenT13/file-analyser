package nl.ricoapon.fileanalyser.internal;

/**
 * Exception that will be thrown when input of the algorithm is incorrect.
 */
public class FileAnalyserConfigurationException extends RuntimeException {
    public FileAnalyserConfigurationException(String message) {
        super(message);
    }
}

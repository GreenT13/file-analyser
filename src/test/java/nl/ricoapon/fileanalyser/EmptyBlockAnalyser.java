package nl.ricoapon.fileanalyser;

import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;

/**
 * Class that needs to be public for the test in {@link nl.ricoapon.fileanalyser.FileAnalyserTest}, because it will
 * be instantiated using reflection. This fails if the class is protected or private.
 */
public class EmptyBlockAnalyser implements BlockAnalyser<String, Integer> {
    @Override
    public void processBlock(String block, Integer storage) {

    }

    @Override
    public Class<Integer> getStorageClass() {
        return null;
    }
}

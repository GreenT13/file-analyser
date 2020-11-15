package nl.ricoapon.fileanalyser.internal;

import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileAnalyserAlgorithmTest {
    private static class Storage {
        public int nrOfA = 0;
        public int nrOfBlocks = 0;
    }

    /** Implementation of {@link BlockAnalyser} that will count how many blocks starts with the letter 'A'. */
    private static class CountNumberOfA implements BlockAnalyser<String, Storage> {
        @Override
        public void processBlock(String block, Storage storage) {
            if (block.startsWith("A")) {
                storage.nrOfA += 1;
            }
        }

        @Override
        public Class<Storage> getStorageClass() {
            return Storage.class;
        }
    }

    /** Implementation of {@link BlockAnalyser} that will count how many blocks are processed. */
    private static class CountBlocks implements BlockAnalyser<String, Storage> {
        @Override
        public void processBlock(String block, Storage storage) {
            storage.nrOfBlocks += 1;
        }

        @Override
        public Class<Storage> getStorageClass() {
            return Storage.class;
        }
    }

    /** Implementation of {@link BlockAnalyser} for which {@link #processBlock(String, Storage)} should never be called. */
    private static class AlwaysSkip implements BlockAnalyser<String, Storage> {
        @Override
        public void processBlock(String block, Storage storage) {
            throw new RuntimeException("This should not happen.");
        }

        @Override
        public boolean shouldProcessBlock(String block, Storage storage) {
            return false;
        }

        @Override
        public Class<Storage> getStorageClass() {
            return Storage.class;
        }
    }

    /**
     * @param input The input.
     * @return {@link Iterator} where each element is a line of the input.
     */
    private static Iterator<String> createLineIterator(String input) {
        return Arrays.stream(input.split("\n")).iterator();
    }

    @Test
    void happyFlow() {
        // Given
        FileAnalyserAlgorithm<String> fileAnalyserAlgorithm = new FileAnalyserAlgorithm<>();
        Iterator<String> blockSupplier = createLineIterator("C\nB\nA");
        List<BlockAnalyser<String, ?>> blockAnalysers = Arrays.asList(new CountBlocks(), new CountNumberOfA(), new AlwaysSkip());
        StorageInstanceContainer storageInstanceContainer = new StorageInstanceContainer(Collections.singletonList(new Storage()));

        // When
        var result = fileAnalyserAlgorithm.execute(blockSupplier, blockAnalysers, storageInstanceContainer);

        // Then
        Storage storage = (Storage) result.get(Storage.class);
        assertThat(storage.nrOfA, equalTo(1));
        assertThat(storage.nrOfBlocks, equalTo(3));
    }

    @Test
    void throwExceptionWhenStorageInstanceWasNotSupplied() {
        // Given
        FileAnalyserAlgorithm<String> fileAnalyserAlgorithm = new FileAnalyserAlgorithm<>();
        Iterator<String> blockSupplier = createLineIterator("A");
        List<BlockAnalyser<String, ?>> blockAnalysers = Collections.singletonList(new CountBlocks());
        StorageInstanceContainer storageInstanceContainer = new StorageInstanceContainer(Collections.emptyList());

        // When and then
        assertThrows(FileAnalyserConfigurationException.class, () -> fileAnalyserAlgorithm.execute(blockSupplier, blockAnalysers, storageInstanceContainer));
    }
}

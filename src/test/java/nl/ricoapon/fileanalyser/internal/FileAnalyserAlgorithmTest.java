package nl.ricoapon.fileanalyser.internal;

import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileAnalyserAlgorithmTest {
    private static class Storage {
        public int nrOfA = 0;
        public int nrOfBlocks = 0;
        public boolean hasFirstBeenProcessed = false;
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
     * @return {@link BlockAnalyserComparatorCreator} that creates a {@link Comparator} that will consider all items equal,
     * meaning sorting results in doing nothing.
     */
    private static BlockAnalyserComparatorCreator<String> dummyComparatorCreator() {
        return blockAnalysers -> (o1, o2) -> 0;
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
        FileAnalyserAlgorithm<String> fileAnalyserAlgorithm = new FileAnalyserAlgorithm<>(dummyComparatorCreator());
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
        FileAnalyserAlgorithm<String> fileAnalyserAlgorithm = new FileAnalyserAlgorithm<>(dummyComparatorCreator());
        Iterator<String> blockSupplier = createLineIterator("A");
        List<BlockAnalyser<String, ?>> blockAnalysers = Collections.singletonList(new CountBlocks());
        StorageInstanceContainer storageInstanceContainer = new StorageInstanceContainer(Collections.emptyList());

        // When and then
        assertThrows(FileAnalyserConfigurationException.class, () -> fileAnalyserAlgorithm.execute(blockSupplier, blockAnalysers, storageInstanceContainer));
    }


    private static class First implements BlockAnalyser<String, Storage> {
        @Override
        public void processBlock(String block, Storage storage) {
            if (storage.hasFirstBeenProcessed) {
                throw new RuntimeException("First was not processed first!");
            }
            storage.hasFirstBeenProcessed = true;
        }

        @Override
        public Class<Storage> getStorageClass() {
            return Storage.class;
        }
    }

    private static class Second implements BlockAnalyser<String, Storage> {
        @Override
        public void processBlock(String block, Storage storage) {
            if (!storage.hasFirstBeenProcessed) {
                throw new RuntimeException("Second not processed after first!");
            }
            storage.hasFirstBeenProcessed = false;
        }

        @Override
        public Class<Storage> getStorageClass() {
            return Storage.class;
        }
    }

    /**
     * @param reversed Indicates if the reversed should be created.
     * @return {@link BlockAnalyserComparatorCreator} that creates a {@link Comparator} that makes sorts
     * {@link First} and {@link Second}. If {@code reversed} is false, {@link First} will come first. If {@code reversed}
     * is true, {@link First} will come last.
     */
    private static BlockAnalyserComparatorCreator<String> firstSecondComparator(boolean reversed) {
        Comparator<BlockAnalyser<String, ?>> comparator = (o1, o2) -> {
            if (o1 instanceof First && o2 instanceof First) {
                return 0;
            }

            if (o1 instanceof Second && o2 instanceof Second) {
                return 0;
            }

            if (o1 instanceof First && o2 instanceof Second) {
                return -1;
            }

            if (o1 instanceof Second && o2 instanceof First) {
                return 1;
            }

            throw new RuntimeException("Only FirstBlockAnalyser and SecondBlockAnalyser should be used.");
        };

        if (reversed) {
            return blockAnalysers -> comparator.reversed();
        } else {
            return blockAnalysers -> comparator;
        }
    }

    @Test
    void orderIsUsedBasedOnComparator() {
        // Given
        var first = new First();
        var second = new Second();
        Collection<BlockAnalyser<String, ?>> blockAnalysers = Arrays.asList(first, second);
        Iterator<String> blockSupplier = createLineIterator("C\nB\nA");
        StorageInstanceContainer storageInstanceContainer = new StorageInstanceContainer(Collections.singletonList(new Storage()));

        // When and then
        // The test is setup in such a way that an exception is thrown when the order is incorrect.
        // So we call execute twice: once with normal ordering one with reverse. This should do the trick.

        // Correct case:
        var fileAnalyserAlgorithm = new FileAnalyserAlgorithm<>(firstSecondComparator(false));
        fileAnalyserAlgorithm.execute(blockSupplier, blockAnalysers, storageInstanceContainer);
        // Incorrect case:
        var fileAnalyserAlgorithm2 = new FileAnalyserAlgorithm<>(firstSecondComparator(true));
        Iterator<String> blockSupplier2 = createLineIterator("C\nB\nA");
        assertThrows(RuntimeException.class, () -> fileAnalyserAlgorithm2.execute(blockSupplier2, blockAnalysers, storageInstanceContainer));
    }
}

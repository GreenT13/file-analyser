package nl.ricoapon.fileanalyser;

import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;
import nl.ricoapon.fileanalyser.internal.FileAnalyserAlgorithm;
import nl.ricoapon.fileanalyser.internal.FileAnalyserConfigurationException;
import nl.ricoapon.fileanalyser.internal.StorageInstanceContainer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileAnalyserTest {
    /**
     * Mock object of {@link FileAnalyserAlgorithm} to check which input parameters are used to call
     * {@link FileAnalyserAlgorithm#execute(Iterator, Collection, StorageInstanceContainer)}.
     */
    private static class MockFileAnalyserAlgorithm extends FileAnalyserAlgorithm<String> {
        public Iterator<String> blockSupplier;
        public Collection<BlockAnalyser<String, ?>> blockAnalysers;
        public Map<Class<?>, Object> storageInstanceMap;

        private Map<Class<?>, Object> returnValue;

        public MockFileAnalyserAlgorithm(Map<Class<?>, Object> returnValue) {
            super(null);
            this.returnValue = returnValue;
        }

        public MockFileAnalyserAlgorithm() {
            super(null);
        }

        @Override
        public Map<Class<?>, Object> execute(Iterator<String> blockSupplier, Collection<BlockAnalyser<String, ?>> blockAnalysers, StorageInstanceContainer storageInstanceContainer) {
            this.blockSupplier = blockSupplier;
            this.blockAnalysers = blockAnalysers;
            this.storageInstanceMap = storageInstanceContainer.toMap();
            return returnValue;
        }
    }

    private static class CannotBeInstantiatedBlockAnalyser implements BlockAnalyser<String, Integer> {
        @Override
        public void processBlock(String block, Integer storage) {
        }

        @Override
        public Class<Integer> getStorageClass() {
            return null;
        }
    }

    private static class EmptyStorage2 {
    }

    private static class EmptyStorage3 {
    }

    private static Iterator<String> dummyIterator() {
        return Arrays.stream("A".split("A")).iterator();
    }

    @Test
    void instancesAreCorrectlyPassed() {
        // Given
        var blockAnalyser1 = new EmptyBlockAnalyser();
        var blockAnalyser2 = new EmptyBlockAnalyser();
        var blockAnalyser3 = new EmptyBlockAnalyser();
        var storage1 = new EmptyStorage();
        var storage2 = new EmptyStorage2();
        var storage3 = new EmptyStorage3();
        var blockSupplier = dummyIterator();
        var mockFileAnalyserAlgorithm = new MockFileAnalyserAlgorithm();

        // When
        new FileAnalyser<>(blockSupplier, mockFileAnalyserAlgorithm)
                .addBlockAnalyser(blockAnalyser1)
                .addBlockAnalysers(Arrays.asList(blockAnalyser2, blockAnalyser3))
                .addStorageInstance(storage1)
                .addStorageInstances(Arrays.asList(storage2, storage3))
                .execute();

        // Then
        assertThat(mockFileAnalyserAlgorithm.blockSupplier, equalTo(blockSupplier));
        assertThat(mockFileAnalyserAlgorithm.blockAnalysers, containsInAnyOrder(blockAnalyser1, blockAnalyser2, blockAnalyser3));
        assertThat(mockFileAnalyserAlgorithm.storageInstanceMap, equalTo(Map.of(
                EmptyStorage.class, storage1,
                EmptyStorage2.class, storage2,
                EmptyStorage3.class, storage3)));
    }

    @Test
    void classesAreCorrectlyPassed() {
        // Given
        var blockSupplier = dummyIterator();
        var mockFileAnalyserAlgorithm = new MockFileAnalyserAlgorithm();

        // When (method with single class as input)
        new FileAnalyser<>(blockSupplier, mockFileAnalyserAlgorithm)
                .addBlockAnalyserClass(EmptyBlockAnalyser.class)
                .addStorageClass(EmptyStorage.class)
                .execute();

        // Then (method with single class as input)
        assertThat(mockFileAnalyserAlgorithm.blockSupplier, equalTo(blockSupplier));
        assertThat(mockFileAnalyserAlgorithm.blockAnalysers.iterator().next(), instanceOf(EmptyBlockAnalyser.class));
        assertThat(mockFileAnalyserAlgorithm.storageInstanceMap.get(EmptyStorage.class), instanceOf(EmptyStorage.class));

        // When (method with collection as input)
        new FileAnalyser<>(blockSupplier, mockFileAnalyserAlgorithm)
                .addBlockAnalyserClasses(Collections.singleton(EmptyBlockAnalyser.class))
                .addStorageClasses(Collections.singleton(EmptyStorage.class))
                .execute();

        // When (method with collection as input)
        assertThat(mockFileAnalyserAlgorithm.blockSupplier, equalTo(blockSupplier));
        assertThat(mockFileAnalyserAlgorithm.blockAnalysers.iterator().next(), instanceOf(EmptyBlockAnalyser.class));
        assertThat(mockFileAnalyserAlgorithm.storageInstanceMap.get(EmptyStorage.class), instanceOf(EmptyStorage.class));
    }

    @Test
    void algorithmReturnValueIsReturned() {
        // Given
        var blockSupplier = dummyIterator();
        Map<Class<?>, Object> returnValue = Map.of();
        var mockFileAnalyserAlgorithm = new MockFileAnalyserAlgorithm(returnValue);

        // When
        var result = new FileAnalyser<>(blockSupplier, mockFileAnalyserAlgorithm)
                .addBlockAnalyserClass(EmptyBlockAnalyser.class)
                .addStorageClass(EmptyStorage.class)
                .execute();

        // Then
        // Note that we cannot use the equal method here. Equal method on maps is overridden to check whether the content
        // is identical. Check using ==, since this way we make sure that it is the same object in memory.
        assertThat(result == returnValue, equalTo(true));
    }

    @Test
    void exceptionIsThrownWhenClassCannotBeInstantiated() {
        // Given
        var blockSupplier = dummyIterator();
        var mockFileAnalyserAlgorithm = new MockFileAnalyserAlgorithm();

        // When and then
        assertThrows(FileAnalyserConfigurationException.class, () -> new FileAnalyser<>(blockSupplier, mockFileAnalyserAlgorithm)
                .addBlockAnalyserClass(CannotBeInstantiatedBlockAnalyser.class));
        assertThrows(FileAnalyserConfigurationException.class, () -> new FileAnalyser<>(blockSupplier, mockFileAnalyserAlgorithm)
                .addStorageClass(EmptyStorage2.class));
    }
}

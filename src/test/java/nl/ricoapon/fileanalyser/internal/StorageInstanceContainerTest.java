package nl.ricoapon.fileanalyser.internal;

import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class StorageInstanceContainerTest {

    private static class Storage1 {
    }

    private static class Storage2 {
    }

    private static class Storage1BlockAnalyser implements BlockAnalyser<Object, Storage1> {
        @Override
        public void processBlock(Object block, Storage1 storage) {
        }

        @Override
        public Class<Storage1> getStorageClass() {
            return Storage1.class;
        }
    }

    private static class Storage2BlockAnalyser implements BlockAnalyser<Object, Storage2> {
        @Override
        public void processBlock(Object block, Storage2 storage) {
        }

        @Override
        public Class<Storage2> getStorageClass() {
            return Storage2.class;
        }
    }

    @Test
    void instanceFromMapIsFound() {
        // Given
        var storage1 = new Storage1();
        var storage2 = new Storage2();
        var storageInstances = Arrays.asList(storage1, storage2);

        // When
        var storageInstanceContainer = new StorageInstanceContainer(storageInstances);
        Storage1 result1 = (Storage1) storageInstanceContainer.getStorageForBlockAnalyser(new Storage1BlockAnalyser()).get();
        Storage2 result2 = (Storage2) storageInstanceContainer.getStorageForBlockAnalyser(new Storage2BlockAnalyser()).get();

        // Then
        assertThat(result1, equalTo(storage1));
        assertThat(result2, equalTo(storage2));
    }

    @Test
    void emptyIsReturnedWhenNotFound() {
        // Given
        var storageInstances = Collections.emptyList();

        // When
        var storageInstanceContainer = new StorageInstanceContainer(storageInstances);
        var result = storageInstanceContainer.getStorageForBlockAnalyser(new Storage1BlockAnalyser());

        // Then
        assertThat(result.isEmpty(), equalTo(true));
    }

    @Test
    void multipleInstancesOfSingleClassThrowsException() {
        // Given
        List<Object> storageInstances = Arrays.asList(new Storage1(), new Storage1());

        // When and then
        assertThrows(FileAnalyserConfigurationException.class, () -> new StorageInstanceContainer(storageInstances));
    }

    @Test
    void toMapGivesCorrectMap() {
        // Given
        var storage1 = new Storage1();
        var storage2 = new Storage2();
        List<Object> storageInstances = Arrays.asList(storage1, storage2);

        // When
        Map<Class<?>, Object> result = new StorageInstanceContainer(storageInstances).toMap();

        // Then
        // Equals of map check whether all entries are identical, meaning copies of maps are identical.
        assertThat(result, equalTo(Map.of(Storage1.class, storage1, Storage2.class, storage2)));
    }

    @Test
    void constructorThrowsNpeWithNullInput() {
        assertThrows(NullPointerException.class, () -> new StorageInstanceContainer(null));
    }
}

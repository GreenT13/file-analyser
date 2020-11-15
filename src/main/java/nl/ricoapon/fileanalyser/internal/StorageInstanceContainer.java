package nl.ricoapon.fileanalyser.internal;

import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Class for storing all the storage instances based on their class.
 * This class is almost immutable: only the content of the storage instances can change.
 */
public class StorageInstanceContainer {
    private final Map<Class<?>, Object> storageInstances;

    /**
     * Creates a container with a copy of the given map.
     * @param storageInstances The map of storage instances.
     */
    public StorageInstanceContainer(Map<Class<?>, Object> storageInstances) {
        Objects.requireNonNull(storageInstances);
        this.storageInstances = new HashMap<>(storageInstances);
    }

    /**
     * Creates a container based on the given list.
     * @param storageInstancesList The list of storage instances.
     * @throws FileAnalyserConfigurationException When more than one instance is supplied for a single class.
     */
    public StorageInstanceContainer(List<Object> storageInstancesList) {
        Objects.requireNonNull(storageInstancesList);
        storageInstances = new HashMap<>();
        for (Object storageInstance : storageInstancesList) {
            Class<?> storageInstanceClass = storageInstance.getClass();
            if (storageInstances.containsKey(storageInstanceClass)) {
                throw new FileAnalyserConfigurationException("For each class, at most one storage instance must be supplied." +
                        "To fix this, remove duplicate instances of the input of this constructor.");
            }

            storageInstances.put(storageInstanceClass, storageInstance);
        }
    }

    /**
     * @param blockAnalyser The block analyser.
     * @return The storage instance for the block analyser.
     */
    public Optional<Object> getStorageForBlockAnalyser(BlockAnalyser<?, ?> blockAnalyser) {
        return Optional.ofNullable(storageInstances.get(blockAnalyser.getStorageClass()));
    }

    /**
     * @return {@link Map} where the keys are the classes of the storage instances and the values are the storage instances.
     */
    public Map<Class<?>, Object> toMap() {
        return new HashMap<>(storageInstances);
    }
}

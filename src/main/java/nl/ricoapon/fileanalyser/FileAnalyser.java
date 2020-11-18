package nl.ricoapon.fileanalyser;

import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;
import nl.ricoapon.fileanalyser.internal.BlockAnalyserOrderComparatorCreator;
import nl.ricoapon.fileanalyser.internal.FileAnalyserAlgorithm;
import nl.ricoapon.fileanalyser.internal.FileAnalyserConfigurationException;
import nl.ricoapon.fileanalyser.internal.ReflectionUtil;
import nl.ricoapon.fileanalyser.internal.StorageInstanceContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Entry point for calling the algorithm described in {@link FileAnalyserAlgorithm#execute(Iterator, Collection, StorageInstanceContainer)}.
 * @param <B> The type of the block.
 */
public class FileAnalyser<B> {
    private final FileAnalyserAlgorithm<B> fileAnalyserAlgorithm;
    private final Iterator<B> blockSupplier;
    private final Collection<BlockAnalyser<B, ?>> blockAnalysers = new ArrayList<>();
    private final Collection<Object> storageInstances = new ArrayList<>();

    FileAnalyser(Iterator<B> blockSupplier, FileAnalyserAlgorithm<B> fileAnalyserAlgorithm) {
        this.blockSupplier = blockSupplier;
        this.fileAnalyserAlgorithm = fileAnalyserAlgorithm;
    }

    /**
     * @param blockSupplier The block supplier.
     * @param <B>           The type of the block.
     * @return {@link FileAnalyser} class which can be used to supply the needed classes and run the algorithm.
     */
    public static <B> FileAnalyser<B> of(Iterator<B> blockSupplier) {
        return new FileAnalyser<>(blockSupplier, new FileAnalyserAlgorithm<>(new BlockAnalyserOrderComparatorCreator<>()));
    }

    /**
     * See {@link #addBlockAnalysers(Collection)}.
     */
    public FileAnalyser<B> addBlockAnalyser(BlockAnalyser<B, ?> blockAnalyser) {
        return addBlockAnalysers(Collections.singleton(blockAnalyser));
    }

    /**
     * Adds a block analysers to the algorithm.
     * @param blockAnalysers The block analysers.
     * @return This object.
     */
    public FileAnalyser<B> addBlockAnalysers(Collection<BlockAnalyser<B, ?>> blockAnalysers) {
        this.blockAnalysers.addAll(blockAnalysers);
        return this;
    }

    /**
     * See {@link #addBlockAnalyserClasses(Collection)}.
     */
    public FileAnalyser<B> addBlockAnalyserClass(Class<? extends BlockAnalyser<B, ?>> blockAnalyserClass) {
        return addBlockAnalyserClasses(Collections.singleton(blockAnalyserClass));
    }

    /**
     * Instantiates block analysers using the no-arg constructor of the classes and adds them to the algorithm.
     * @param blockAnalyserClasses The classes of the block analysers.
     * @return This object.
     * @throws FileAnalyserConfigurationException If the instance could not be created.
     */
    public FileAnalyser<B> addBlockAnalyserClasses(Collection<Class<? extends BlockAnalyser<B, ?>>> blockAnalyserClasses) {
        this.blockAnalysers.addAll(blockAnalyserClasses.stream()
                .map(ReflectionUtil::instantiate).collect(Collectors.toList()));
        return this;
    }

    /**
     * See {@link #addStorageInstances(Collection)}.
     */
    public FileAnalyser<B> addStorageInstance(Object storageInstance) {
        return addStorageInstances(Collections.singleton(storageInstance));
    }

    /**
     * Adds storage instances to the algorithm.
     * @param storageInstances The storage instances.
     * @return This object.
     */
    public FileAnalyser<B> addStorageInstances(Collection<Object> storageInstances) {
        this.storageInstances.addAll(storageInstances);
        return this;
    }

    /**
     * See {@link #addStorageClasses(Collection)}.
     */
    public FileAnalyser<B> addStorageClass(Class<?> storageClass) {
        return addStorageClasses(Collections.singleton(storageClass));
    }

    /**
     * Instantiates storage instances using the no-arg constructor of the classes and adds them to the algorithm.
     * @param storageClasses The storage classes.
     * @return This object.
     */
    public FileAnalyser<B> addStorageClasses(Collection<Class<?>> storageClasses) {
        this.storageInstances.addAll(storageClasses.stream()
                .map(ReflectionUtil::instantiate).collect(Collectors.toList()));
        return this;
    }

    /**
     * See {@link FileAnalyserAlgorithm#execute(Iterator, Collection, StorageInstanceContainer)}.
     */
    public Map<Class<?>, Object> execute() {
        return fileAnalyserAlgorithm.execute(blockSupplier, blockAnalysers, new StorageInstanceContainer(storageInstances));
    }
}

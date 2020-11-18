package nl.ricoapon.fileanalyser.internal;

import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class containing the algorithm to analyse a file. See {@link #execute(Iterator, Collection, StorageInstanceContainer)} for more details.
 * @param <B> The type of the block.
 */
public class FileAnalyserAlgorithm<B> {
    /** Creates a {@link Comparator} for {@link BlockAnalyser}s. Used as input variable so it can be mocked when testing. */
    private final BlockAnalyserComparatorCreator<B> blockAnalyserComparatorCreator;

    public FileAnalyserAlgorithm(BlockAnalyserComparatorCreator<B> blockAnalyserComparatorCreator) {
        this.blockAnalyserComparatorCreator = blockAnalyserComparatorCreator;
    }

    /**
     * Calls {@link BlockAnalyser#processBlock(Object, Object)} for each of the blocks supplied. The storage
     * object should be modified. The list of block analysers will be sorted using the given {@link #blockAnalyserComparatorCreator}.
     * @param blockSupplier            The supplier of blocks.
     * @param blockAnalysers           The objects that analyse the blocks.
     * @param storageInstanceContainer Container with all the storage instances.
     * @return Map with the classes of the storage instances as key and the instances corresponding to that class as value.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map<Class<?>, Object> execute(Iterator<B> blockSupplier, Collection<BlockAnalyser<B, ?>> blockAnalysers, StorageInstanceContainer storageInstanceContainer) {
        List<BlockAnalyser<B, ?>> blockAnalyserList = new ArrayList<>(blockAnalysers);
        blockAnalyserList.sort(blockAnalyserComparatorCreator.create(blockAnalysers));

        blockSupplier.forEachRemaining(block -> {
            for (BlockAnalyser blockAnalyser : blockAnalyserList) {
                Optional<Object> storage = storageInstanceContainer.getStorageForBlockAnalyser(blockAnalyser);

                if (storage.isEmpty()) {
                    throw new FileAnalyserConfigurationException("No storage instance of class " + blockAnalyser.getStorageClass() +
                            " could be found for " + blockAnalyser.getClass().getName() + ". " +
                            "Supply exactly storage instance to fix this issue.");
                }

                if (blockAnalyser.shouldProcessBlock(block, storage.get())) {
                    blockAnalyser.processBlock(block, storage.get());
                }
            }
        });

        return storageInstanceContainer.toMap();
    }
}

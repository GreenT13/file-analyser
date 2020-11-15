package nl.ricoapon.fileanalyser.internal;

import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class containing the algorithm to analyse a file. See {@link #execute(Iterator, List, StorageInstanceContainer)} for more details.
 * @param <B> The type of the block.
 */
public class FileAnalyserAlgorithm<B> {
    /**
     * Calls {@link BlockAnalyser#processBlock(Object, Object)} for each of the blocks supplied. The storage
     * object should be modified
     * @param blockSupplier            The supplier of blocks.
     * @param blockAnalysers           The objects that analyse the blocks.
     * @param storageInstanceContainer Container with all the storage instances
     * @return Map with all the storage instances
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map<Class<?>, Object> execute(Iterator<B> blockSupplier, List<BlockAnalyser<B, ?>> blockAnalysers, StorageInstanceContainer storageInstanceContainer) {
        blockSupplier.forEachRemaining(block -> {
            for (BlockAnalyser blockAnalyser : blockAnalysers) {
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

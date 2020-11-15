package nl.ricoapon.fileanalyser.internal;

import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;

import java.util.Iterator;
import java.util.List;

/**
 * Class containing the algorithm to analyse a file. See {@link #execute(Iterator, List, Object)} for more details.
 * @param <B> The type of the block.
 * @param <S> The type of the storage.
 */
public class FileAnalyserAlgorithm<B, S> {
    /**
     * Calls {@link BlockAnalyser#processBlock(Object, Object)} for each of the blocks supplied. The storage
     * object should be modified
     * @param blockSupplier The supplier of blocks.
     * @param blockAnalysers The objects that analyse the blocks.
     * @param storage The storage object containing the result.
     */
    public void execute(Iterator<B> blockSupplier, List<BlockAnalyser<B, S>> blockAnalysers, S storage) {
        blockSupplier.forEachRemaining(block ->
                blockAnalysers.forEach(blockAnalyser -> {
                    if (blockAnalyser.shouldProcessBlock(block, storage)) {
                        blockAnalyser.processBlock(block, storage);
                    }
                }));
    }
}

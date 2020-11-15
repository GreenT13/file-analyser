package nl.ricoapon.fileanalyser.internal;

import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;

import java.util.Collection;
import java.util.Comparator;

/**
 * Interface for creating a {@link Comparator} for {@link BlockAnalyser}.
 * @param <B> The type of the block.
 */
public interface BlockAnalyserComparatorCreator<B> {
    /**
     * @param blockAnalysers The block analysers.
     * @return {@link Comparator} for sorting the block analysers.
     */
    Comparator<BlockAnalyser<B, ?>> create(Collection<BlockAnalyser<B, ?>> blockAnalysers);
}

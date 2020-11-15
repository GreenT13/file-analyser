package nl.ricoapon.fileanalyser.analyser;

/**
 * Interface for classes that will analyse blocks of data.
 * @param <B> The type of the block.
 * @param <S> The type of the storage.
 */
public interface BlockAnalyser<B, S> {
    /**
     * Extracts the needed information from the {@code block} and stores relevant details into the {@code storage}.
     * @param block The block to process.
     * @param storage The object containing data that will be the result of the algorithm.
     */
    void processBlock(B block, S storage);

    /**
     * @param block The block.
     * @param storage The storage.
     * @return If {@link #processBlock(Object, Object)} should be called for the given data.
     */
    @SuppressWarnings("unused")
    default boolean shouldProcessBlock(B block, S storage) {
        return true;
    }
}

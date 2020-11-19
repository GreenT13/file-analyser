package nl.ricoapon.fileanalyser;

import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;
import nl.ricoapon.fileanalyser.analyser.BlockAnalyserOrder;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

/**
 * Integration test of the algorithm without mocking any objects.
 */
public class AlgorithmIntegrationTest {

    private enum BlockType {
        START, LOG, END
    }

    private static class Block {
        public final int rowNr;
        public final BlockType blockType;
        public final Timestamp tsLogged;

        public Block(int rowNr, BlockType blockType, Timestamp tsLogged) {
            this.rowNr = rowNr;
            this.blockType = blockType;
            this.tsLogged = tsLogged;
        }
    }

    private static class Storage {
        public Timestamp tsStart;
        public final List<Long> durations = new ArrayList<>();
    }

    /** Fills {@link Storage#tsStart} if the {@link Block} is of type {@link BlockType#START}. */
    private static class StartBlockFinder implements BlockAnalyser<Block, Storage> {
        @Override
        public void processBlock(Block block, Storage storage) {
            if (BlockType.START.equals(block.blockType) && storage.tsStart != null) {
                throw new RuntimeException("A second block started while the first block didn't end yet");
            } else if (BlockType.START.equals(block.blockType)) {
                storage.tsStart = block.tsLogged;
            }
        }

        @Override
        public Class<Storage> getStorageClass() {
            return Storage.class;
        }
    }

    /** Fills {@link Storage#durations} with the durations between start and end block. */
    @BlockAnalyserOrder(after = StartBlockFinder.class)
    private static class EndBlockFinder implements BlockAnalyser<Block, Storage> {
        @Override
        public void processBlock(Block block, Storage storage) {
            if (BlockType.END.equals(block.blockType) && storage.tsStart == null) {
                throw new RuntimeException("A block started ended without starting");
            } else if (BlockType.END.equals(block.blockType)) {
                // tsStart should already be filled by StartRecordFinder
                storage.durations.add(block.tsLogged.getTime() - storage.tsStart.getTime());

                storage.tsStart = null;
            }
        }

        @Override
        public Class<Storage> getStorageClass() {
            return Storage.class;
        }
    }

    @Test
    void integrationTest() {
        // Given
        List<BlockAnalyser<Block, ?>> blockAnalysers = Arrays.asList(new StartBlockFinder(), new EndBlockFinder());
        var storage = new Storage();
        Iterator<Block> blockSupplier = Arrays.asList(
                new Block(1, BlockType.START, Timestamp.valueOf("2000-01-01 00:00:01")),
                new Block(1, BlockType.LOG, Timestamp.valueOf("2000-01-01 00:00:02")),
                new Block(1, BlockType.END, Timestamp.valueOf("2000-01-01 00:00:03")),
                new Block(1, BlockType.START, Timestamp.valueOf("2000-01-01 00:00:04")),
                new Block(1, BlockType.LOG, Timestamp.valueOf("2000-01-01 00:00:05")),
                new Block(1, BlockType.LOG, Timestamp.valueOf("2000-01-01 00:00:06")),
                new Block(1, BlockType.END, Timestamp.valueOf("2000-01-01 00:00:07"))
                ).iterator();

        // When
        Storage result = (Storage) FileAnalyser.of(blockSupplier)
                .addBlockAnalysers(blockAnalysers)
                .addStorageInstance(storage)
                .execute()
                .get(Storage.class);

        // Then
        assertThat(result, equalTo(storage));
        assertThat(result.durations, contains(2000L, 3000L));
    }
}

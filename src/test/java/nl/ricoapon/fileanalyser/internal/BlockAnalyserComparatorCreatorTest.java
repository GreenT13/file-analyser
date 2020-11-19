package nl.ricoapon.fileanalyser.internal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;
import nl.ricoapon.fileanalyser.analyser.BlockAnalyserOrder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressFBWarnings(value = "SIC_INNER_SHOULD_BE_STATIC", justification = "@Nested classes should be non-static, but SpotBugs wants them static." +
        "See https://github.com/spotbugs/spotbugs/issues/560 for the bug (open since 2018).")
class BlockAnalyserComparatorCreatorTest {
    private final BlockAnalyserOrderComparatorCreator<String> blockAnalyserComparatorCreator = new BlockAnalyserOrderComparatorCreator<>();

    /**
     * Basic class that can be extended to avoid boiler plate of block analysers or to instantiate empty block analysers.
     */
    private static class BasicBlockAnalyser implements BlockAnalyser<String, String> {
        @Override
        public void processBlock(String block, String storage) {

        }

        @Override
        public Class<String> getStorageClass() {
            return String.class;
        }
    }


    @Nested
    class CyclicRelation {

        @BlockAnalyserOrder(after = CyclicAnalyser3.class)
        private class CyclicAnalyser1 extends BasicBlockAnalyser {
        }

        @BlockAnalyserOrder(after = CyclicAnalyser1.class)
        private class CyclicAnalyser2 extends BasicBlockAnalyser {
        }

        @BlockAnalyserOrder(after = CyclicAnalyser2.class)
        private class CyclicAnalyser3 extends BasicBlockAnalyser {

        }

        @Test
        public void cyclicDependencyIsDetected() {
            // Given
            List<BlockAnalyser<String, ?>> blockAnalysers = Arrays.asList(new CyclicAnalyser1(), new CyclicAnalyser2(), new CyclicAnalyser3());

            // When and then
            assertThrows(FileAnalyserConfigurationException.class, () -> blockAnalyserComparatorCreator.create(blockAnalysers));
        }
    }

    @Nested
    class DiamondRelation {
        private class First extends BasicBlockAnalyser {
        }

        @BlockAnalyserOrder(after = First.class)
        private class Middle1 extends BasicBlockAnalyser {
        }

        @BlockAnalyserOrder(after = First.class)
        private class Middle2 extends BasicBlockAnalyser {
        }

        @BlockAnalyserOrder(after = {Middle1.class, Middle2.class})
        private class Last extends BasicBlockAnalyser {
        }

        @Test
        public void diamondDependencyIsCorrectlyCompared() {
            // Given
            var first = new First();
            var middle1 = new Middle1();
            var middle2 = new Middle2();
            var last = new Last();
            List<BlockAnalyser<String, ?>> blockAnalysers = Arrays.asList(middle1, last, middle2, first);

            // When
            blockAnalysers.sort(blockAnalyserComparatorCreator.create(blockAnalysers));

            // Then
            // Note that two elements that are considered equal (middle1 and middle2) will never be reordered.
            assertThat(blockAnalysers, contains(first, middle1, middle2, last));
        }

        @Test
        public void differentInstancesOfTheSameClassHaveNoRelation() {
            // Given
            var first1 = new First();
            var first2 = new First();
            var middle1 = new Middle1();
            var middle2 = new Middle2();
            var last = new Last();
            List<BlockAnalyser<String, ?>> blockAnalysers = Arrays.asList(last, middle1, first1, middle2, first2);

            // When
            blockAnalysers.sort(blockAnalyserComparatorCreator.create(blockAnalysers));

            // Then
            // Note that two elements that are considered equal (firstX and middleX) will never be reordered.
            assertThat(blockAnalysers, contains(first1, first2, middle1, middle2, last));
        }

        @Test
        void comparatorValueIsReversedWhenInputIsReversed() {
            // Given
            var first = new First();
            var middle = new Middle1();
            List<BlockAnalyser<String, ?>> blockAnalysers = Arrays.asList(first, middle);
            Comparator<BlockAnalyser<String, ?>> comparator = blockAnalyserComparatorCreator.create(blockAnalysers);

            // When and then
            assertThat(comparator.compare(first, middle), equalTo(-1));
            assertThat(comparator.compare(middle, first), equalTo(1));
        }
    }

    @Nested
    class NoRelation {
        private class A extends BasicBlockAnalyser {
        }

        @BlockAnalyserOrder
        private class B extends BasicBlockAnalyser {
        }

        @Test
        public void nonRelatedBlockAnalysersHaveNoRelation() {
            // Given
            var a = new A();
            var b = new B();
            List<BlockAnalyser<String, ?>> blockAnalysers = Arrays.asList(a, b);

            // When
            blockAnalysers.sort(blockAnalyserComparatorCreator.create(blockAnalysers));

            // Then
            // Note that two elements that have no relation will never be reordered.
            assertThat(blockAnalysers, contains(a, b));
        }

        @Test
        public void equalBlockAnalysersHaveNoRelation() {
            // Given
            var blockAnalyser = new BasicBlockAnalyser();

            // When
            var comparator = blockAnalyserComparatorCreator.create(Collections.singleton(blockAnalyser));

            // Then
            // List with two identical items is not testable, so we test the comparator directly.
            assertThat("The block analysers are equal, so result should be zero.",
                    comparator.compare(blockAnalyser, blockAnalyser), equalTo(0));
        }
    }
}

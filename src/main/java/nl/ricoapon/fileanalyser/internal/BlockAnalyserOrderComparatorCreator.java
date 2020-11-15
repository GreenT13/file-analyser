package nl.ricoapon.fileanalyser.internal;

import nl.ricoapon.fileanalyser.analyser.BlockAnalyser;
import nl.ricoapon.fileanalyser.analyser.BlockAnalyserOrder;
import org.jgrapht.alg.shortestpath.DijkstraManyToManyShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.builder.GraphBuilder;

import java.util.Collection;
import java.util.Comparator;

/**
 * Creates {@link Comparator} for {@link BlockAnalyser} objects.
 */
public class BlockAnalyserOrderComparatorCreator {

    private BlockAnalyserOrderComparatorCreator() {
        // This class is not meant to be initialized.
    }

    /**
     * Creates a {@link Comparator} that preserves the order defined by {@link BlockAnalyserOrder}.
     * This comes down to a {@link Comparator} compares A and B in such a way that:
     * <ul>
     *     <li>A is larger than B when A should be processed after B.</li>
     *     <li>A is equal to B when they are the same instances or there is processing order defined between A and B.</li>
     * </ul>
     * @param blockAnalysers The list of block analysers.
     * @return {@link Comparator}
     */
    public static Comparator<BlockAnalyser<?, ?>> createPreservingBlockAnalyserOrder(Collection<BlockAnalyser<?, ?>> blockAnalysers) {
        DirectedAcyclicGraph<BlockAnalyser<?, ?>, DefaultEdge> directedAcyclicGraph = createGraph(blockAnalysers);

        // Create a shortest path object using any algorithm. Dijkstra was chosen because it is the most popular.
        var shortestPathsAlgorithm = new DijkstraManyToManyShortestPaths<>(directedAcyclicGraph);
        var shortestPaths = shortestPathsAlgorithm.getManyToManyPaths(directedAcyclicGraph.vertexSet(), directedAcyclicGraph.vertexSet());

        return (blockAnalyserA, blockAnalyserB) -> {
            // If a path is calculated between two vertices that are equal, an empty path (!= null) is returned.
            // To make sure that we return 0 for these vertices, we catch this case beforehand.
            if (blockAnalyserA == blockAnalyserB) {
                return 0;
            }

            if (shortestPaths.getPath(blockAnalyserA, blockAnalyserB) != null) {
                // There is a path from A to B, meaning A should be after B, meaning A is larger than B.
                return 1;
            }

            if (shortestPaths.getPath(blockAnalyserB, blockAnalyserA) != null) {
                // There is a path from B to A, meaning A should be before B, meaning A is smaller than B.
                return -1;
            }

            // Different instances with no path between them. This means they are considered equal.
            return 0;
        };
    }

    /**
     * Creates a graph where the vertices are the given block analysers and the edges are determined by {@link BlockAnalyserOrder}.
     * An edge (A,B) represents that B should be processed after A (in comparator terms: B is larger than A).
     * @param blockAnalysers The block analysers.
     * @return A directed acyclic graph.
     * @throws FileAnalyserConfigurationException If a cyclic relation occurs in the graph.
     */
    private static DirectedAcyclicGraph<BlockAnalyser<?, ?>, DefaultEdge> createGraph(Collection<BlockAnalyser<?, ?>> blockAnalysers) {
        // The full type must be spelled out (instead of using var) to avoid compile errors.
        GraphBuilder<BlockAnalyser<?, ?>, DefaultEdge, ? extends DirectedAcyclicGraph<BlockAnalyser<?, ?>, DefaultEdge>> graphBuilder
                = DirectedAcyclicGraph.createBuilder(DefaultEdge.class);

        // Adding an edge also automatically adds a vertex. However, to make sure that all vertices are present, we manually
        // add all block analysers as a vertex.
        blockAnalysers.forEach(graphBuilder::addVertex);

        for (BlockAnalyser<?, ?> blockAnalyserA : blockAnalysers) {
            for (BlockAnalyser<?, ?> blockAnalyserB : blockAnalysers) {
                if (shouldABeAfterBWithoutTransitive(blockAnalyserA, blockAnalyserB)) {
                    // The graph will throw an IllegalArgumentException if an edge would create a cycle.
                    // Wrap this exception to make it more clear to the user what to do.
                    try {
                        graphBuilder.addEdge(blockAnalyserA, blockAnalyserB);
                    } catch (IllegalArgumentException e) {
                        throw new FileAnalyserConfigurationException("A cyclic dependency was found. Adding the edge (" + blockAnalyserA.getClass().getName()
                                + ", " + blockAnalyserB.getClass().getName() + " completed the circle. " +
                                "Please fix this issue by correcting the value @BlockAnalyserOrder#after().", e);
                    }
                }
            }
        }

        return graphBuilder.build();
    }

    /**
     * @param blockAnalyserA The first block analysers.
     * @param blockAnalyserB The second block analysers.
     * @return Whether {@code blockAnalyserA} has the class of {@code blockAnalyserB} in {@link BlockAnalyserOrder#after()}.
     * This does not take transitive relations into account.
     */
    @SuppressWarnings("rawtypes")
    private static boolean shouldABeAfterBWithoutTransitive(BlockAnalyser<?, ?> blockAnalyserA, BlockAnalyser<?, ?> blockAnalyserB) {
        BlockAnalyserOrder annotation = blockAnalyserA.getClass().getAnnotation(BlockAnalyserOrder.class);
        if (annotation == null) {
            return false;
        }

        Class<? extends BlockAnalyser>[] parentClasses = annotation.after();
        if (parentClasses.length == 0) {
            return false;
        }

        // Check if one of the mentioned classes is EXACTLY the same as the parent classes.
        // If so, we have a direct parent child relation.
        for (Class<? extends BlockAnalyser> bpClass : parentClasses) {
            if (bpClass.equals(blockAnalyserB.getClass())) {
                return true;
            }
        }
        return false;
    }
}

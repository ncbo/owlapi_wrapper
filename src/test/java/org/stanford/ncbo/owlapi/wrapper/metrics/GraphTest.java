package org.stanford.ncbo.owlapi.wrapper.metrics;

import org.junit.Test;
import org.stanford.ncbo.owlapi.wrapper.util.TableDrivenGraph;
import org.stanford.ncbo.owlapi.wrapper.util.TableDrivenNode;

import java.util.*;

import static org.junit.Assert.*;

public class GraphTest {

    @Test
    public void testSimple() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("a", Set.of("b"));
        map.put("b", Set.of("c"));
        map.put("c", Set.of());

        TableDrivenGraph<String> graph = TableDrivenGraph.getGraph(map);
        assertEquals(2, graph.maxDepth());
        graph.clearCaches();
        assertEquals(2, new TableDrivenNode<>(graph, "a").maxDepth());
        graph.clearCaches();
        assertEquals(1, new TableDrivenNode<>(graph, "b").maxDepth());
        graph.clearCaches();
        assertEquals(0, new TableDrivenNode<>(graph, "c").maxDepth());
    }

    @Test
    public void testOneLoop01() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("a", Set.of("b"));
        map.put("b", Set.of("c","d"));
        map.put("c", Set.of("a","e"));
        map.put("d", Set.of("f"));
        map.put("e", Set.of("g"));
        map.put("f", Set.of());
        map.put("g", Set.of());

        TableDrivenGraph<String> g = TableDrivenGraph.getGraph(map);
        assertEquals(2, g.maxDepth());
        g.clearCaches();
        assertEquals(2, new TableDrivenNode<String>(g, "c").maxDepth());
        g.clearCaches();
        assertEquals(2, new TableDrivenNode<String>(g, "b").maxDepth());
    }

    @Test
    public void testOneLoop02() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("a", Set.of("b"));
        map.put("b", Set.of("c","d"));
        map.put("c", Set.of("a","e"));
        map.put("d", Set.of("f"));
        map.put("e", Set.of("g"));
        map.put("f", Set.of("h"));
        map.put("g", Set.of());
        map.put("h", Set.of());

        TableDrivenGraph<String> g = TableDrivenGraph.getGraph(map);
        assertEquals(3, g.maxDepth());
        g.clearCaches();
        assertEquals(3, new TableDrivenNode<String>(g, "c").maxDepth());
        g.clearCaches();
        assertEquals(3, new TableDrivenNode<String>(g, "b").maxDepth());
    }

    @Test
    public void testOneLoop03() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("a", Set.of("b"));
        map.put("b", Set.of("c","d"));
        map.put("c", Set.of("a","e"));
        map.put("d", Set.of("f"));
        map.put("e", Set.of("g"));
        map.put("f", Set.of());
        map.put("g", Set.of("h"));
        map.put("h", Set.of());

        TableDrivenGraph<String> g = TableDrivenGraph.getGraph(map);
        assertEquals(3, g.maxDepth());
        g.clearCaches();
        assertEquals(3, new TableDrivenNode<String>(g, "c").maxDepth());
        g.clearCaches();
        assertEquals(3, new TableDrivenNode<String>(g, "b").maxDepth());
    }
    @Test
    public void testValid() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("a", Set.of("b"));
        map.put("b", Set.of("c", "d"));
        map.put("c", Set.of("a", "e"));
        map.put("d", Set.of("f"));
        map.put("e", Set.of("g"));
        map.put("f", Set.of());
        map.put("g", Set.of("h"));
        map.put("h", Set.of());
        TableDrivenGraph<String> graph = TableDrivenGraph.getGraph(map);

        boolean ex1Found=false;
        try {
            new TableDrivenNode<>(graph, "c");
        }
        catch (IllegalArgumentException e) {
            ex1Found=true;
        }
        assertFalse(ex1Found);

        boolean ex2Found=false;
        try {
            new TableDrivenNode<>(graph, "z");
        }
        catch (IllegalArgumentException e) {
            ex2Found=true;
        }
        assertTrue(ex2Found);
    }

    /*
     *     a
     *           c
     *     b  =  d
     *     e
     */
    @Test
    public void testEquivTransfer01() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("a", Set.of("b", "c"));
        map.put("b", Set.of("d", "e"));
        map.put("c", Set.of("d"));
        map.put("d", Set.of("b"));
        map.put("e", Set.of());
        TableDrivenGraph<String> graph = TableDrivenGraph.getGraph(map);

        assertEquals(3,graph.maxDepth());
    }

    /*
     *     a
     *     b
     *     c  =  d
     *           e
     */
    @Test
    public void testEquivTransfer02() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("a", Set.of("b", "d"));
        map.put("b", Set.of("c"));
        map.put("c", Set.of("d"));
        map.put("d", Set.of("c", "e"));
        map.put("e", Set.of());
        TableDrivenGraph<String> graph = TableDrivenGraph.getGraph(map);

        assertEquals(3,graph.maxDepth());
    }

    @Test
    public void testTrivialLoop01() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("a", Set.of("b"));
        map.put("b", Set.of("a"));
        TableDrivenGraph<String> graph = TableDrivenGraph.getGraph(map);

        assertEquals(0, graph.maxDepth());
    }

    @Test
    public void testTrivialLoop02() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("a", Set.of("b"));
        map.put("b", Set.of("a", "c"));
        map.put("c", Set.of());
        TableDrivenGraph<String> graph = TableDrivenGraph.getGraph(map);

        assertEquals(1, graph.maxDepth());
    }

    @Test
    public void testTrivialLoop03() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("a", Set.of("b", "c"));
        map.put("b", Set.of("a"));
        map.put("c", Set.of());
        TableDrivenGraph<String> graph = TableDrivenGraph.getGraph(map);

        assertEquals(1, graph.maxDepth());
    }

    @Test
    public void testTrivialLoop04() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("a", Set.of("a"));
        TableDrivenGraph<String> graph = TableDrivenGraph.getGraph(map);

        assertEquals(0, graph.maxDepth());
    }

    @Test
    public void testTrivialLoop05() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("a", Set.of("a", "b"));
        map.put("b", Set.of());;
        TableDrivenGraph<String> graph = TableDrivenGraph.getGraph(map);

        assertEquals(1, graph.maxDepth());
    }
}


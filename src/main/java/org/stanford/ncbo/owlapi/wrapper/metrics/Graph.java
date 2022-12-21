package org.stanford.ncbo.owlapi.wrapper.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class Graph {
    private static final Logger logger = LoggerFactory.getLogger(Graph.class);

    final private Map<Node, Set<Node>> equivalenceClasses=new HashMap<>();
    final private Map<Node, Integer> depthMap = new HashMap<>();

    public abstract Set<Node> getSubNodes(Node x);

    public abstract Set<Node> getNodes();



    protected void initEquivalenceClasses() {
        for (Node x : getNodes()) {
             Set<Node> s = new HashSet<>();
             s.add(x);
            equivalenceClasses.put(x, s);
        }
        Set<Node> seen = new HashSet<>();
        for (Node x : getNodes()) {
            List<Node> path = new ArrayList<>();

            initEquivalenceClasses(x, seen, path);
        }
    }

    private void initEquivalenceClasses(Node x, Set<Node> seen, List<Node> path) {
        if (!seen.contains(x)) {
            seen.add(x);
            path.add(x);

            for (Node y : getSubNodes(x)) {
                initEquivalenceClasses(y, seen, path);
            }

            path.remove(x);
        } else {
            int i = 0;
            int sz = path.size();
            for (Node z : path) {
                if (isEquivalent(x,z)) {
                    merge(z, path.subList(i,sz));
                    break;
                }
                i++;
            }
        }
    }

    public boolean isEquivalent(Node x, Node y) {
        return equivalenceClasses.get(x) != null && equivalenceClasses.get(x).contains(y);
    }

    private void merge(Node x, List<Node> path) {
        if (logger.isDebugEnabled()) {
            logger.debug("Found loop starting at " + x + " along " + path);
        }
        for (Node y : path) {
            merge(x, y);
        }
    }

    private void merge(Node x, Node y) {
        Set<Node> xEquivalenceClass = equivalenceClasses.get(x);
        Set<Node> yEquivalenceClass = equivalenceClasses.get(y);
        if (xEquivalenceClass != yEquivalenceClass) {
            xEquivalenceClass.addAll(yEquivalenceClass);
            equivalenceClasses.put(y, xEquivalenceClass);
        }
    }

    boolean checkNode(Node x) {
        return equivalenceClasses.get(x) != null;
    }

    public int maxDepth() {
        int depth = 0;
        for (Node x : getNodes()) {
            depth = Math.max(depth, maxDepth(x));
        }
        return depth;
    }

    int maxDepth(Node x) {
        Integer depth;
        if ((depth = depthMap.get(x)) != null) {
            return depth;
        }
        Set<Node> set = new HashSet<>();
        set.add(x);
        depth = maxDepth(x, new HashSet<>());
        setDepth(x, depth);
        return depth;
    }

    private void setDepth(Node x, int depth) {
        if (logger.isDebugEnabled()) {
            logger.debug("depth for " + x + " is " + depth);
        }
        for (Node y : equivalenceClasses.get(x)) {
            depthMap.put(x, depth);
        }
    }

    private int maxDepth(Node x, Set<Node> seen) {
        int depth = 0;
        for (Node y : getSubNodes(x)) {
            if (!isEquivalent(x, y)) {
                depth = Math.max(depth, maxDepth(y) + 1);
            }
        }
        for (Node y : getSubNodes(x)) {
            if (isEquivalent(x, y) && !seen.contains(y)) {
                seen.add(y);
                depth = Math.max(depth, maxDepth(y,seen));
            }
        }
        return depth;
    }

    public void clearCaches() {
        depthMap.clear();
    }
}
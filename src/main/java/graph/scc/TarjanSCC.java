package graph.scc;

import graph.model.Edge;
import graph.util.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Tarjan's SCC algorithm.
 * Input: adjacency lists as List<List<Edge>> for 0..n-1
 * Output: list of components; each component is List<Integer> (nodes)
 */
public class TarjanSCC {
    private final List<List<Edge>> adj;
    private final int n;

    public TarjanSCC(List<List<Edge>> adj) {
        this.adj = adj;
        this.n = adj.size();
    }

    public List<List<Integer>> run(Metrics metrics) {
        int[] index = new int[n];
        int[] low = new int[n];
        boolean[] onStack = new boolean[n];
        for (int i = 0; i < n; i++) index[i] = -1;

        Deque<Integer> stack = new ArrayDeque<>();
        List<List<Integer>> sccs = new ArrayList<>();
        final int[] curIndex = {0}; // wrap in array to mutate in lambda

        metrics.startTimer();
        for (int v = 0; v < n; v++) {
            if (index[v] == -1) {
                strongconnect(v, index, low, onStack, stack, sccs, curIndex, metrics);
            }
        }
        metrics.stopTimer();
        return sccs;
    }

    private void strongconnect(int v, int[] index, int[] low, boolean[] onStack,
                               Deque<Integer> stack, List<List<Integer>> sccs, int[] curIndex,
                               Metrics metrics) {
        index[v] = curIndex[0];
        low[v] = curIndex[0];
        curIndex[0]++;
        stack.push(v);
        onStack[v] = true;
        metrics.inc("tarjan_push");

        for (Edge e : adj.get(v)) {
            metrics.inc("tarjan_edge_visited");
            int w = e.v;
            if (index[w] == -1) {
                strongconnect(w, index, low, onStack, stack, sccs, curIndex, metrics);
                low[v] = Math.min(low[v], low[w]);
            } else if (onStack[w]) {
                low[v] = Math.min(low[v], index[w]);
            }
        }

        if (low[v] == index[v]) {
            List<Integer> comp = new ArrayList<>();
            while (true) {
                int w = stack.pop();
                onStack[w] = false;
                metrics.inc("tarjan_pop");
                comp.add(w);
                if (w == v) break;
            }
            sccs.add(comp);
        }
    }
}

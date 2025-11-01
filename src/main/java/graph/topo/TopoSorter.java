package graph.topo;

import graph.model.Edge;
import graph.util.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Kahn topological sorter for a DAG adjacency list.
 */
public class TopoSorter {
    private final List<List<Edge>> adj;
    private final int n;

    public TopoSorter(List<List<Edge>> adj) {
        this.adj = adj;
        this.n = adj.size();
    }

    public List<Integer> kahn(Metrics metrics) {
        int[] indeg = new int[n];
        for (int u = 0; u < n; u++) {
            for (Edge e : adj.get(u)) {
                indeg[e.v]++;
            }
        }

        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) q.add(i);

        List<Integer> topo = new ArrayList<>();
        metrics.startTimer();
        while (!q.isEmpty()) {
            int u = q.poll();
            topo.add(u);
            metrics.inc("kahn_pops");
            for (Edge e : adj.get(u)) {
                indeg[e.v]--;
                metrics.inc("kahn_edge_relax");
                if (indeg[e.v] == 0) {
                    q.add(e.v);
                    metrics.inc("kahn_pushes");
                }
            }
        }
        metrics.stopTimer();
        return topo;
    }
}

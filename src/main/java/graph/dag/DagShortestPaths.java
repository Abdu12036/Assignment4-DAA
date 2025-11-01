package graph.dag;

import graph.model.Edge;
import graph.util.Metrics;
import java.util.Arrays;
import java.util.List;

/**
 * Single-source the shortest paths on a DAG using topological order relaxations.
 */
public class DagShortestPaths {
    private final List<List<Edge>> adj;
    private final int n;

    public DagShortestPaths(List<List<Edge>> adj) {
        this.adj = adj;
        this.n = adj.size();
    }

    public static class Result {
        public final long[] dist;
        public final int[] prev;

        public Result(long[] dist, int[] prev) {
            this.dist = dist;
            this.prev = prev;
        }
    }

    /**
     * compute the shortest distances from srcComp using topo order (topo must be a topological order).
     */
    public Result shortestFrom(int srcComp, List<Integer> topo, Metrics metrics) {
        final long INF = Long.MAX_VALUE / 4;
        long[] dist = new long[n];
        int[] prev = new int[n];
        Arrays.fill(dist, INF);
        Arrays.fill(prev, -1);
        dist[srcComp] = 0;

        metrics.startTimer();
        for (int u : topo) {
            if (dist[u] == INF) continue;
            for (Edge e : adj.get(u)) {
                int v = e.v;
                long w = e.w;
                metrics.inc("dagsp_relax_attempt");
                if (dist[v] > dist[u] + w) {
                    dist[v] = dist[u] + w;
                    prev[v] = u;
                    metrics.inc("dagsp_relax_success");
                }
            }
        }
        metrics.stopTimer();
        return new Result(dist, prev);
    }

    /** reconstruct path of component ids from srcComp to targetComp using prev array. */
    public static int[] reconstruct(int srcComp, int targetComp, int[] prev) {
        if (prev == null) return null;
        if (srcComp == targetComp) return new int[]{srcComp};
        java.util.List<Integer> seq = new java.util.ArrayList<>();
        int cur = targetComp;
        while (cur != -1) {
            seq.add(cur);
            if (cur == srcComp) break;
            cur = prev[cur];
        }
        if (seq.get(seq.size() - 1) != srcComp) return null;
        int[] out = new int[seq.size()];
        for (int i = 0; i < seq.size(); i++) out[i] = seq.get(seq.size() - 1 - i);
        return out;
    }
}

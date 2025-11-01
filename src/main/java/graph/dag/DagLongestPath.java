package graph.dag;

import graph.model.Edge;
import graph.util.Metrics;
import java.util.Arrays;
import java.util.List;

/**
 * Longest path on DAG via DP over topological ordering.
 * Initializes sources (zero indegree) to 0 and computes max distances.
 */
public class DagLongestPath {
    private final List<List<Edge>> adj;
    private final int n;

    public DagLongestPath(List<List<Edge>> adj) {
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

    public Result longestOverTopo(List<Integer> topo, int[] indeg, Metrics metrics) {
        final long NEG_INF = Long.MIN_VALUE / 4;
        long[] dist = new long[n];
        int[] prev = new int[n];
        Arrays.fill(dist, NEG_INF);
        Arrays.fill(prev, -1);

        // initialize indeg==0 nodes to 0 (sources)
        for (int i = 0; i < n; i++) {
            if (indeg[i] == 0) dist[i] = 0;
        }

        metrics.startTimer();
        for (int u : topo) {
            if (dist[u] == NEG_INF) continue;
            for (Edge e : adj.get(u)) {
                int v = e.v;
                long w = e.w;
                metrics.inc("daglong_relax_attempt");
                if (dist[v] < dist[u] + w) {
                    dist[v] = dist[u] + w;
                    prev[v] = u;
                    metrics.inc("daglong_relax_success");
                }
            }
        }
        metrics.stopTimer();
        return new Result(dist, prev);
    }

    public static int[] reconstruct(int end, int[] prev) {
        if (prev == null) return null;
        java.util.List<Integer> seq = new java.util.ArrayList<>();
        int cur = end;
        while (cur != -1) {
            seq.add(cur);
            cur = prev[cur];
        }
        int[] out = new int[seq.size()];
        for (int i = 0; i < seq.size(); i++) out[i] = seq.get(seq.size() - 1 - i);
        return out;
    }
}

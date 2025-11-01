package graph.condense;

import graph.model.Edge;
import java.util.*;

/**
 * Build condensation DAG from original edges and SCC mapping.
 * If multiple original edges go from component A to B, we keep the minimal weight.
 */
public class Condensation {
    public final int compCount;
    public final List<List<Edge>> condAdj;
    public final Map<Integer, List<Integer>> compToNodes; // component id -> original nodes

    /**
     * @param n total nodes
     * @param sccs list of components (each is list of nodes)
     * @param origEdges original edges list
     */
    public Condensation(int n, List<List<Integer>> sccs, List<Edge> origEdges) {
        this.compCount = sccs.size();
        compToNodes = new HashMap<>();
        for (int i = 0; i < sccs.size(); i++) {
            compToNodes.put(i, new ArrayList<>(sccs.get(i)));
        }

        // node -> comp id
        int[] compId = new int[n];
        Arrays.fill(compId, -1);
        for (int i = 0; i < sccs.size(); i++) {
            for (int node : sccs.get(i)) compId[node] = i;
        }

        // build minimal weight edges between components
        Map<Long, Long> minWeight = new HashMap<>(); // key = (cu<<32)|cv -> weight
        for (Edge e : origEdges) {
            int cu = compId[e.u];
            int cv = compId[e.v];
            if (cu == cv) continue;
            long key = (((long) cu) << 32) | (cv & 0xffffffffL);
            long w = e.w;
            if (!minWeight.containsKey(key) || w < minWeight.get(key)) {
                minWeight.put(key, w);
            }
        }

        condAdj = new ArrayList<>();
        for (int i = 0; i < compCount; i++) condAdj.add(new ArrayList<>());
        for (Map.Entry<Long, Long> kv : minWeight.entrySet()) {
            long key = kv.getKey();
            int cu = (int) (key >>> 32);
            int cv = (int) (key & 0xffffffffL);
            condAdj.get(cu).add(new Edge(cu, cv, kv.getValue()));
        }
    }
}

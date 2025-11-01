package graph.model;

import java.util.List;

/**
 * Deserializable structure for tasks.json.
 * Fields match the user's JSON (directed, n, edges, source, weight_model).
 */
public class GraphInput {
    public boolean directed;
    public int n;
    public List<JsonEdge> edges;
    public Integer source;
    public String weight_model;

    public static class JsonEdge {
        public int u;
        public int v;
        public long w;
    }
}

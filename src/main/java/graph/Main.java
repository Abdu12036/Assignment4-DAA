package graph;

import graph.model.Edge;
import graph.scc.TarjanSCC;
import graph.condense.Condensation;
import graph.topo.TopoSorter;
import graph.dag.DagShortestPaths;
import graph.dag.DagLongestPath;
import graph.util.Metrics;
import graph.util.SimpleMetrics;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Main pipeline for Assignment 4.
 * Reads datasets from /data/, runs all algorithms, and writes results to /results/.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Path dataDir = Paths.get("data");
        Path resultsDir = Paths.get("results");
        if (!Files.exists(resultsDir)) Files.createDirectories(resultsDir);

        // Process all JSON files in /data/
        try (var files = Files.list(dataDir)) {
            files.filter(p -> p.toString().endsWith(".json"))
                    .sorted()
                    .forEach(path -> {
                        try {
                            processDataset(path, resultsDir);
                        } catch (Exception e) {
                            System.err.println("❌ Error processing " + path + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
        }
        System.out.println("\n✅ All datasets processed. See /results/");
    }

    // ----------------------------------------------------------------------

    private static void processDataset(Path path, Path resultsDir) throws IOException {
        System.out.println("\n=== Running on " + path.getFileName() + " ===");
        GraphData g = parseJson(path);
        SimpleMetrics metrics = new SimpleMetrics();

        // Build adjacency list
        List<List<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < g.n; i++) adj.add(new ArrayList<>());
        for (Edge e : g.edges) adj.get(e.u).add(e);

        long startTotal = System.nanoTime();

        // 1. Tarjan SCC
        var sccs = new TarjanSCC(adj).run((Metrics) metrics);
        int sccCount = sccs.size();

        // 2. Condensation DAG
        var cond = new Condensation(g.n, sccs, g.edges);

        // 3. Topological sort
        var topo = new TopoSorter(cond.condAdj).kahn((Metrics) metrics);

        // 4. DAG shortest paths
        int srcComp = cond.nodeToComp[g.source];
        var dsp = new DagShortestPaths(cond.condAdj);
        var resSP = dsp.shortestFrom(srcComp, topo, (Metrics) metrics);

        // 5. DAG longest path
        int[] indeg = new int[cond.compCount];
        for (int u = 0; u < cond.compCount; u++)
            for (Edge e : cond.condAdj.get(u)) indeg[e.v]++;
        var dlp = new DagLongestPath(cond.condAdj);
        var resLP = dlp.longestOverTopo(topo, indeg, (Metrics) metrics);

        long endTotal = System.nanoTime();
        double totalMs = (endTotal - startTotal) / 1e6;

        // Write summary
        Path outFile = resultsDir.resolve("result_" + path.getFileName().toString().replace(".json", ".txt"));
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(outFile))) {
            pw.printf("Dataset: %s%n", path.getFileName());
            pw.printf("Vertices: %d, Edges: %d%n", g.n, g.edges.size());
            pw.printf("SCC count: %d%n%n", sccCount);

            pw.println("=== Condensation DAG edges ===");
            for (int u = 0; u < cond.compCount; u++) {
                for (Edge e : cond.condAdj.get(u)) {
                    pw.printf("  Comp%d -> Comp%d (w=%d)%n", e.u, e.v, e.w);
                }
            }
            pw.println();

            pw.println("=== Topological Order ===");
            pw.println(topo);
            pw.println();

            pw.printf("Source vertex: %d (component %d)%n%n", g.source, srcComp);

            pw.println("=== Shortest Path Distances from Source Component ===");
            for (int i = 0; i < resSP.dist.length; i++) {
                pw.printf("  To Comp%d : %s%n", i,
                        (resSP.dist[i] >= Long.MAX_VALUE / 8 ? "unreachable" : resSP.dist[i] + ""));
            }
            pw.println();

            pw.println("=== Longest Path (Critical Path) Distances ===");
            for (int i = 0; i < resLP.dist.length; i++) {
                pw.printf("  To Comp%d : %s%n", i,
                        (resLP.dist[i] <= Long.MIN_VALUE / 8 ? "N/A" : resLP.dist[i] + ""));
            }
            pw.println();

            pw.printf("Total time: %.3f ms%n", totalMs);
            pw.printf("Operation count: %d%n", metrics.getOps());
        }

        System.out.printf("%s processed — %d SCCs, %.2f ms%n",
                path.getFileName(), sccCount, totalMs);
    }

    // ----------------------------------------------------------------------

    /** Minimal JSON parser for the dataset format (no external libraries). */
    private static GraphData parseJson(Path path) throws IOException {
        String text = Files.readString(path).replaceAll("[\\n\\r]", "");
        GraphData g = new GraphData();
        g.directed = text.contains("\"directed\": true");
        g.n = extractInt(text, "\"n\"");
        g.source = extractInt(text, "\"source\"");
        g.weightModel = extractString(text, "\"weight_model\"");
        g.edges = new ArrayList<>();

        String edgesStr = text.substring(text.indexOf("\"edges\""));
        String[] parts = edgesStr.split("\\{");
        for (String part : parts) {
            if (!part.contains("\"u\"")) continue;
            int u = extractInt(part, "\"u\"");
            int v = extractInt(part, "\"v\"");
            int w = extractInt(part, "\"w\"");
            g.edges.add(new Edge(u, v, w));
        }
        return g;
    }

    private static int extractInt(String text, String key) {
        int idx = text.indexOf(key);
        if (idx < 0) return 0;
        int colon = text.indexOf(':', idx);
        int comma = text.indexOf(',', colon);
        if (comma < 0) comma = text.indexOf('}', colon);
        return Integer.parseInt(text.substring(colon + 1, comma).replaceAll("[^0-9-]", "").trim());
    }

    private static String extractString(String text, String key) {
        int idx = text.indexOf(key);
        if (idx < 0) return "";
        int colon = text.indexOf(':', idx);
        int quote1 = text.indexOf('"', colon + 1);
        int quote2 = text.indexOf('"', quote1 + 1);
        return text.substring(quote1 + 1, quote2);
    }

    // ----------------------------------------------------------------------

    private static class GraphData {
        boolean directed;
        int n;
        List<Edge> edges;
        int source;
        String weightModel;
    }
}

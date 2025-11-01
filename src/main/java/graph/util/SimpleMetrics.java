package graph.util;

import java.util.*;

/**
 * SimpleMetrics implements Metrics interface.
 * Tracks timing and arbitrary counters by name.
 */
public class SimpleMetrics implements Metrics {

    private long startTime;
    private long elapsed;
    private final Map<String, Integer> counters = new HashMap<>();

    // --- Timing ---
    @Override
    public void startTimer() {
        startTime = System.nanoTime();
    }

    @Override
    public void stopTimer() {
        elapsed += System.nanoTime() - startTime;
    }

    @Override
    public long elapsedNano() {
        return elapsed;
    }

    // --- Counters ---
    @Override
    public void inc(String name) {
        counters.merge(name, 1, Integer::sum);
    }

    @Override
    public int getCount(String name) {
        return counters.getOrDefault(name, 0);
    }

    @Override
    public Map<String, Integer> getAllCounts() {
        return Collections.unmodifiableMap(counters);
    }

    // --- Helpers ---
    public void reset() {
        counters.clear();
        elapsed = 0;
    }

    public long getOps() {
        return counters.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void report(String label) {
        System.out.printf("%s: time=%.3f ms, totalOps=%d%n",
                label, elapsed / 1e6, getOps());
    }
}

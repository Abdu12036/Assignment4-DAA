package graph.util;

import java.util.HashMap;
import java.util.Map;

public class SimpleMetrics implements Metrics {
    private long start = 0;
    private long end = 0;
    private final Map<String, Integer> counters = new HashMap<>();

    @Override
    public void startTimer() {
        start = System.nanoTime();
    }

    @Override
    public void stopTimer() {
        end = System.nanoTime();
    }

    @Override
    public long elapsedNano() {
        if (start == 0) return 0;
        return (end == 0 ? System.nanoTime() : end) - start;
    }

    @Override
    public void inc(String counterName) {
        counters.put(counterName, counters.getOrDefault(counterName, 0) + 1);
    }

    @Override
    public int getCount(String counterName) {
        return counters.getOrDefault(counterName, 0);
    }

    @Override
    public Map<String, Integer> getAllCounts() {
        return Map.copyOf(counters);
    }
}

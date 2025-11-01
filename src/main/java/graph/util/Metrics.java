package graph.util;

import java.util.Map;

public interface Metrics {
    void startTimer();
    void stopTimer();
    long elapsedNano();

    void inc(String counterName);
    int getCount(String counterName);
    Map<String, Integer> getAllCounts();
}

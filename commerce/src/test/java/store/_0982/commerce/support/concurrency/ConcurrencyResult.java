package store._0982.commerce.support.concurrency;

import java.util.Collections;
import java.util.List;

public record ConcurrencyResult(long durationMs, int successCount, int failCount, List<Exception> exceptions, List<Long> latenciesNs) {

    public ConcurrencyResult {
        exceptions = (exceptions == null) ? List.of() : Collections.unmodifiableList(exceptions);
        latenciesNs = (latenciesNs == null) ? List.of() : Collections.unmodifiableList(latenciesNs);
    }

    public double tps() {
        long d = Math.max(durationMs, 1);
        return (successCount + failCount) * 1000.0 / d;
    }

    public double p95Ms() {
        return percentileMs(95);
    }

    public double p99Ms() {
        return percentileMs(99);
    }

    private double percentileMs(int p) {
        if (latenciesNs == null || latenciesNs.isEmpty()) return 0.0;

        long[] arr = latenciesNs.stream().mapToLong(Long::longValue).sorted().toArray();
        int idx = (int) Math.ceil((p / 100.0) * arr.length) - 1;
        idx = Math.max(0, Math.min(idx, arr.length - 1));
        return arr[idx] / 1_000_000.0;
    }
}

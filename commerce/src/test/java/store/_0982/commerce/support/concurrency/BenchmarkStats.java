package store._0982.commerce.support.concurrency;

import java.util.List;

public record BenchmarkStats(String name, long minMs, long maxMs, double avgMs, double medianMs, double stdDeviation,
                             List<ConcurrencyResult> runs) {

    public static BenchmarkStats from(String name, List<ConcurrencyResult> results) {
        List<Long> durations = results.stream()
                .map(ConcurrencyResult::durationMs)
                .sorted()
                .toList();

        long min = durations.get(0);
        long max = durations.get(durations.size() - 1);
        double avg = durations.stream().mapToLong(Long::longValue).average().orElse(0);
        double median = durations.get(durations.size() / 2);
        double stdDev = calculateStdDeviation(durations, avg);

        return new BenchmarkStats(name, min, max, avg, median, stdDev, results);
    }

    public void printReport() {
        System.out.println("\n" + "-".repeat(50));
        System.out.println("[벤치마크 결과] " + name + "\n");
        System.out.printf("최소: %dms%n", minMs);
        System.out.printf("최대: %dms%n", maxMs);
        System.out.printf("평균: %.1fms%n", avgMs);
        System.out.printf("중앙값: %.1fms%n", medianMs);
        System.out.printf("표준편차: %.2fms%n", stdDeviation);
        System.out.printf("실행 횟수: %d회%n", runs.size());
        System.out.println("-".repeat(50) + "\n");
    }

    private static double calculateStdDeviation(List<Long> values, double mean) {
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }
}

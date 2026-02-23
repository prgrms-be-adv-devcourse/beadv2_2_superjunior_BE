package store._0982.commerce.support.concurrency;

import java.util.List;

public record BenchmarkStats(
        String name,
        long minMs,
        long maxMs,
        double avgMs,
        double medianMs,
        double stdDeviation,
        double avgTps,
        double avgP95Ms,
        double avgP99Ms,
        double overallP95Ms,
        double overallP99Ms,
        List<ConcurrencyResult> runs) {

    public static BenchmarkStats from(String name, List<ConcurrencyResult> results) {
        // Duration 통계
        List<Long> durations = results.stream()
                .map(ConcurrencyResult::durationMs)
                .sorted()
                .toList();

        long min = durations.get(0);
        long max = durations.get(durations.size() - 1);
        double avg = durations.stream().mapToLong(Long::longValue).average().orElse(0);
        double median = durations.get(durations.size() / 2);
        double stdDev = calculateStdDeviation(durations, avg);

        // TPS 평균
        double avgTps = results.stream()
                .mapToDouble(ConcurrencyResult::tps)
                .average()
                .orElse(0);

        // 각 run의 p95/p99 평균
        double avgP95 = results.stream()
                .mapToDouble(ConcurrencyResult::p95Ms)
                .average()
                .orElse(0);

        double avgP99 = results.stream()
                .mapToDouble(ConcurrencyResult::p99Ms)
                .average()
                .orElse(0);

        // 전체 latencies를 모아서 통합 p95/p99 계산
        List<Long> allLatenciesNs = results.stream()
                .flatMap(r -> r.latenciesNs().stream())
                .sorted()
                .toList();

        double overallP95 = calculatePercentileMs(allLatenciesNs, 95);
        double overallP99 = calculatePercentileMs(allLatenciesNs, 99);

        return new BenchmarkStats(name, min, max, avg, median, stdDev, avgTps, avgP95, avgP99, overallP95, overallP99, results);
    }

    public void printReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("[벤치마크 결과] " + name);
        System.out.println("=".repeat(60));

        System.out.println("\n[실행 시간 통계]");
        System.out.printf("  최소: %dms%n", minMs);
        System.out.printf("  최대: %dms%n", maxMs);
        System.out.printf("  평균: %.1fms%n", avgMs);
        System.out.printf("  중앙값: %.1fms%n", medianMs);
        System.out.printf("  표준편차: %.2fms%n", stdDeviation);
        System.out.printf("  실행 횟수: %d회%n", runs.size());

        System.out.println("\n[처리량]");
        System.out.printf("  평균 TPS: %.2f req/s%n", avgTps);

        System.out.println("\n[레이턴시 - 각 run 평균]");
        System.out.printf("  평균 p95: %.2fms%n", avgP95Ms);
        System.out.printf("  평균 p99: %.2fms%n", avgP99Ms);

        System.out.println("\n[레이턴시 - 전체 요청 통합]");
        System.out.printf("  전체 p95: %.2fms%n", overallP95Ms);
        System.out.printf("  전체 p99: %.2fms%n", overallP99Ms);

        int totalRequests = runs.stream()
                .mapToInt(r -> r.successCount() + r.failCount())
                .sum();
        System.out.printf("  총 요청 수: %d건%n", totalRequests);

        System.out.println("\n" + "=".repeat(60) + "\n");
    }

    private static double calculateStdDeviation(List<Long> values, double mean) {
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }

    private static double calculatePercentileMs(List<Long> latenciesNs, int percentile) {
        if (latenciesNs == null || latenciesNs.isEmpty()) {
            return 0.0;
        }

        int idx = (int) Math.ceil((percentile / 100.0) * latenciesNs.size()) - 1;
        idx = Math.max(0, Math.min(idx, latenciesNs.size() - 1));
        return latenciesNs.get(idx) / 1_000_000.0;
    }
}

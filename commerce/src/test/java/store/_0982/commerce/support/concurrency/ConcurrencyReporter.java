package store._0982.commerce.support.concurrency;

import java.util.stream.Collectors;

public final class ConcurrencyReporter {

    private ConcurrencyReporter() {}

    public static void printSummary(String testName, ConcurrencyResult result) {
        System.out.println("\n" + "-".repeat(50));
        System.out.println("[동시성 테스트 결과] " + testName + "\n");
        System.out.println("소요 시간: " + result.durationMs() + "ms");
        System.out.println("성공: " + result.successCount() + " / 실패: " + result.failCount());
        System.out.println("TPS: " + String.format("%.2f", result.tps()));

        if (result.latenciesNs() != null && !result.latenciesNs().isEmpty()) {
            System.out.println("p95: " + String.format("%.2f", result.p95Ms()) + "ms");
            System.out.println("p99: " + String.format("%.2f", result.p99Ms()) + "ms");
        } else {
            System.out.println("p95/p99: (latenciesNs 수집 없음)");
        }

        if (!result.exceptions().isEmpty()) {
            System.out.println("-".repeat(50));
            System.out.println("예외 발생: " + result.exceptions().size() + "건");

            var exceptionCounts = result.exceptions().stream()
                    .collect(Collectors.groupingBy(
                            e -> e.getClass().getSimpleName(),
                            Collectors.counting()
                    ));

            exceptionCounts.forEach((type, count) ->
                    System.out.println("  - " + type + ": " + count + "회")
            );
        }
        System.out.println("-".repeat(50) + "\n");
    }
}

package store._0982.commerce.support;

import org.junit.jupiter.api.AfterEach;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseConcurrencyTest extends BaseIntegrationTest{

    private ExecutorService executorService;

    @AfterEach
    void baseTearDown() {
        if(executorService != null && !executorService.isShutdown()){
            executorService.shutdown();
        }
    }

    /**
     * Race Condition 테스트 - 모든 스레드가 정확히 같은 순간 시작 (스레드 개수 = 작업 개수)
     *
     * @param concurrentCount 동시 실행할 개수 (스레드 개수 = 작업 개수)
     * @param task 실행할 작업
     */
    protected ConcurrencyResult runSynchronizedTask(int concurrentCount, Runnable task) throws InterruptedException {
        return runConcurrentTest(concurrentCount, concurrentCount, task);
    }

    /**
     * 부하 테스트 - 고정된 스레드 풀이 많은 작업을 처리 (실제 서버 환경 시뮬레이션)
     *
     * @param threadCount 고정 스레드 개수 (예: 32개)
     * @param taskCount 처리할 작업 개수 (예: 1000개)
     * @param task 실행할 작업
     */
    protected ConcurrencyResult runLoadTest(int threadCount, int taskCount, Runnable task) throws InterruptedException {
        return runConcurrentTest(threadCount, taskCount, task);
    }

    private ConcurrencyResult runConcurrentTest(int threadCount, int taskCount, Runnable task) throws InterruptedException {
        // 기존 스레드 풀 종료
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }

        // threadCount만큼 스레드 풀 생성
        executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);  // 모든 스레드 준비 완료 대기
        CountDownLatch startLatch = new CountDownLatch(1);            // 동시 시작 신호
        CountDownLatch doneLatch = new CountDownLatch(taskCount);     // 모든 작업 완료 대기

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger taskIndex = new AtomicInteger(0);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        // threadCount개 스레드를 모두 대기 상태로 만듦
        for(int i=0; i<threadCount; i++){
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    while(true) {
                        int currentIndex = taskIndex.getAndIncrement();
                        if(currentIndex >= taskCount) {
                            break;
                        }

                        try {
                            task.run();
                            successCount.incrementAndGet();
                        } catch(Exception e) {
                            System.err.println(e.getMessage());
                            exceptions.add(e);
                            failCount.incrementAndGet();
                        } finally {
                            doneLatch.countDown();
                        }
                    }
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        readyLatch.await();
        long startTime = System.currentTimeMillis();
        startLatch.countDown();

        doneLatch.await();
        long duration = System.currentTimeMillis() - startTime;

        return new ConcurrencyResult(duration, successCount.get(), failCount.get(), exceptions);
    }

    public static class ConcurrencyResult {
        private final long durationMs;
        private final int successCount;
        private final int failCount;
        private final List<Exception> exceptions;

        public ConcurrencyResult(long durationMs, int successCount, int failCount, List<Exception> exceptions) {
            this.durationMs = durationMs;
            this.successCount = successCount;
            this.failCount = failCount;
            this.exceptions = exceptions;
        }

        // Getter
        public long getDurationMs() {
            return durationMs;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public List<Exception> getExceptions() {
            return exceptions;
        }

        public boolean isAllSuccess() {
            return failCount == 0;
        }

        // 결과 출력
        public void printSummary(String testName) {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("[동시성 테스트 결과] " + testName);
            System.out.println("=".repeat(80));
            System.out.println("소요 시간: " + durationMs + "ms");
            System.out.println("성공: " + successCount + " / 실패: " + failCount);
            System.out.println("TPS: " + String.format("%.2f", (successCount + failCount) * 1000.0 / durationMs));

            if (!exceptions.isEmpty()) {
                System.out.println("-".repeat(80));
                System.out.println("예외 발생: " + exceptions.size() + "건");
                var exceptionCounts = exceptions.stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                e -> e.getClass().getSimpleName(),
                                java.util.stream.Collectors.counting()
                        ));
                exceptionCounts.forEach((type, count) ->
                        System.out.println("  - " + type + ": " + count + "회")
                );
            }
            System.out.println("=".repeat(80) + "\n");
        }
    }
}

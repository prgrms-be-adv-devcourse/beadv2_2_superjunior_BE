package store._0982.commerce.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseConcurrencyTest extends BaseIntegrationTest{

    private static final int DEFAULT_THREAD_COUNT = 10;

    private ExecutorService executorService;
    private CountDownLatch readyLatch;
    private CountDownLatch startLatch;
    private CountDownLatch doneLatch;

    private int threadCount;

    @BeforeEach
    void baseSetup() {
        initializeConcurrencyContext(getDefaultThreadCount());
    }

    @AfterEach
    void baseTearDown() {
        if(executorService != null && !executorService.isShutdown()){
            executorService.shutdown();
        }
    }

    protected int getDefaultThreadCount() {
        return DEFAULT_THREAD_COUNT;
    }

    protected ConcurrencyResult runSynchronizedTask(Runnable task) throws InterruptedException{
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        long startTime = System.currentTimeMillis();
        for(int i=0;i<threadCount;i++){
            submitSingle(task, successCount, failCount, exceptions);
        }

        awaitAllAndRun();
        long duration = System.currentTimeMillis() - startTime;
        return new ConcurrencyResult(duration, successCount.get(), failCount.get(), exceptions);
    }

    protected ConcurrencyResult runSynchronizedTask(int customThreadCount, Runnable task) throws InterruptedException {
        resetConcurrencyContext(customThreadCount);
        return runSynchronizedTask(task);
    }

    protected void initializeConcurrencyContext(int threadCount){
        this.threadCount = threadCount;
        executorService = Executors.newFixedThreadPool(threadCount);
        readyLatch = new CountDownLatch(threadCount);
        startLatch = new CountDownLatch(1);
        doneLatch = new CountDownLatch(threadCount);
    }

    private void resetConcurrencyContext(int threadCount) {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        initializeConcurrencyContext(threadCount);
    }

    private void submitSingle(Runnable task,
                              AtomicInteger successCount,
                              AtomicInteger failCount,
                              List<Exception> exceptions
    ){
        executorService.submit(() -> {
            try{
                signalReadyAndAwaitStart();
                task.run();
                successCount.incrementAndGet();
            } catch(Exception e){
                System.err.println(e.getMessage());
                exceptions.add(e);
                failCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        });
    }

    private void signalReadyAndAwaitStart() throws InterruptedException {
        readyLatch.countDown();
        startLatch.await();
    }

    private void awaitAllAndRun() throws InterruptedException{
        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
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

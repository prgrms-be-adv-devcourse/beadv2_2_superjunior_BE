package store._0982.commerce.support;

import org.junit.jupiter.api.AfterEach;
import store._0982.commerce.support.concurrency.ConcurrencyResult;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseConcurrencyTest extends BaseIntegrationTest {

    private ExecutorService executorService;

    @AfterEach
    void baseTearDown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    protected ConcurrencyResult runSynchronizedTask(int concurrentCount, Runnable task) throws InterruptedException {
        return  runConcurrentTest(concurrentCount, concurrentCount, task);
    }

    protected ConcurrencyResult runLoadTest(int threadCount, int taskCount, Runnable task) throws InterruptedException {
        return  runConcurrentTest(threadCount, taskCount, task);
    }

    private ConcurrencyResult runConcurrentTest(int threadCount, int taskCount, Runnable task) throws InterruptedException {
        // 기존 스레드 풀 종료
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }

        executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(taskCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger taskIndex = new AtomicInteger(0);

        List<Exception> exceptions = new CopyOnWriteArrayList<>();
        List<Long> latenciesNs = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    while (true) {
                        int currentIndex = taskIndex.getAndIncrement();
                        if (currentIndex >= taskCount) break;

                        long t0 = System.nanoTime();
                        try {
                            task.run();
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            exceptions.add(e);
                            failCount.incrementAndGet();
                        } finally {
                            long t1 = System.nanoTime();
                            latenciesNs.add(t1 - t0);
                            doneLatch.countDown();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        readyLatch.await();

        long startNs = System.nanoTime();
        startLatch.countDown();

        doneLatch.await();
        long durationMs = (System.nanoTime() - startNs) / 1_000_000;

        return new ConcurrencyResult(durationMs, successCount.get(), failCount.get(), exceptions, latenciesNs);
    }
}

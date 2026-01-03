package store._0982.point.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class BaseConcurrencyTest extends BaseIntegrationTest {

    private static final int DEFAULT_THREAD_COUNT = 10;

    private ExecutorService executorService;
    private CountDownLatch readyLatch;
    private CountDownLatch startLatch;
    private CountDownLatch doneLatch;

    private int threadCount;

    @BeforeEach
    void baseSetUp() {
        initializeConcurrencyContext(getDefaultThreadCount());
    }

    @AfterEach
    void baseTearDown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    protected int getDefaultThreadCount() {
        return DEFAULT_THREAD_COUNT;
    }

    protected void runSynchronizedTask(Runnable task) throws InterruptedException {
        for (int i = 0; i < threadCount; i++) {
            submitSingle(task);
        }
        awaitAllAndRun();
    }

    protected void initializeConcurrencyContext(int threadCount) {
        this.threadCount = threadCount;
        executorService = Executors.newFixedThreadPool(threadCount);
        readyLatch = new CountDownLatch(threadCount);
        startLatch = new CountDownLatch(1);
        doneLatch = new CountDownLatch(threadCount);
    }

    protected <T> void runSynchronizedTasks(Collection<T> items, Consumer<T> action) throws InterruptedException {
        if (items.size() != threadCount) {
            throw new IllegalArgumentException("Task 수가 threadCount와 일치해야 합니다: " + threadCount);
        }
        for (T item : items) {
            submitSingle(() -> action.accept(item));
        }
        awaitAllAndRun();
    }

    private void submitSingle(Runnable task) {
        executorService.submit(() -> {
            try {
                signalReadyAndAwaitStart();
                task.run();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            } finally {
                doneLatch.countDown();
            }
        });
    }

    private void signalReadyAndAwaitStart() throws InterruptedException {
        readyLatch.countDown();
        startLatch.await();
    }

    private void awaitAllAndRun() throws InterruptedException {
        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
    }
}

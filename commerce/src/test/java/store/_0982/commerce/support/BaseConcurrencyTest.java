package store._0982.commerce.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    protected void runSynchronizedTask(Runnable task) throws InterruptedException{
        for(int i=0;i<threadCount;i++){
            submitSingle(task);
        }

        awaitAllAndRun();
    }

    protected void initializeConcurrencyContext(int threadCount){
        this.threadCount = threadCount;
        executorService = Executors.newFixedThreadPool(threadCount);
        readyLatch = new CountDownLatch(threadCount);
        startLatch = new CountDownLatch(1);
        doneLatch = new CountDownLatch(threadCount);
    }

    private void submitSingle(Runnable task){
        executorService.submit(() -> {
            try{
                signalReadyAndAwaitStart();
                task.run();  // 예외를 task 안에서 처리하게 함
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted: " + e.getMessage());
            } finally {
                doneLatch.countDown();
            }
        });
    }

    private void signalReadyAndAwaitStart() throws InterruptedException {
        readyLatch.countDown();
        startLatch.await();;
    }

    private void awaitAllAndRun() throws InterruptedException{
        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
    }
}

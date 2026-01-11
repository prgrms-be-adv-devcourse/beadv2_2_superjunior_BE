package store._0982.elasticsearch.application.support;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

//카프카 메시지가 실제로 소비됐는지 확인
public class KafkaTestProbe {

    private final AtomicReference<CountDownLatch> latch =
            new AtomicReference<>(new CountDownLatch(1));

    public void markConsumed() {
        latch.get().countDown();
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return latch.get().await(timeout, unit);
    }

    public void reset() {
        latch.set(new CountDownLatch(1));
    }
}


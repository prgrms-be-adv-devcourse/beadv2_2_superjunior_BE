package store._0982.elasticsearch.application.support;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

//카프카 메시지가 실제로 소비됐는지 확인
public class KafkaTestProbe {

    private final CountDownLatch latch = new CountDownLatch(1);

    public void markConsumed() {
        latch.countDown();
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return latch.await(timeout, unit);
    }
}

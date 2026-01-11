package store._0982.elasticsearch.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.common.exception.CustomException;

@SuppressWarnings("java:S112")
@Component
@RequiredArgsConstructor
public class ElasticsearchExecutor {
    private final ElasticsearchExceptionTranslator translator;

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    public <T> T execute(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw translator.translate(e);
        }
    }

    public void execute(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw translator.translate(e);
        }
    }
}

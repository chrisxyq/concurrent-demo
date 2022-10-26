package basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Slf4j
class CallableThread implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        log.info("come in call method(),current thread:{}", Thread.currentThread().getName());
        return 1080;
    }
}

@Slf4j
public class FutureTaskTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        FutureTask<Integer> futureTask = new FutureTask(new CallableThread());
        new Thread(futureTask, "A").start();
        while (!futureTask.isDone()) {
            log.info("wait...");
        }
        log.info(String.valueOf(futureTask.get()));
    }
}

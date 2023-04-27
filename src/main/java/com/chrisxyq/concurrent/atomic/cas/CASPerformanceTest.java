package com.chrisxyq.concurrent.atomic.cas;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试cas累加的性能
 * 使用硬件同步原语来代替锁
 * 这种方法它只适合于线程之间碰撞不太频繁，也就是说绝大部分情况下，执行 CAS 原语不需要重试这样的场景
 */
@Slf4j
public class CASPerformanceTest {
    private final  AtomicInteger  atomicTotal = new AtomicInteger(0);
    private static Integer        syncTotal   = 0;
    private final  CountDownLatch latch       = new CountDownLatch(10000);
    ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10000, 10000, 1,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    @Test
    public void testAtomic() throws InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (int i = 0; i < 10000; i++) {
            executor.execute(() -> {
                while (!atomicTotal.compareAndSet(atomicTotal.get(),
                        atomicTotal.get() + 1)) {
                }
                latch.countDown();
            });
        }
        latch.await();
        log.info(String.valueOf(atomicTotal.get()));
        log.info("消耗：" + stopWatch.getTime() + "ms");
    }

    @Test
    public void testSync() throws InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < 10000; i++) {
            executor.execute(() -> {
                synchronized (CASPerformanceTest.class) {
                    syncTotal++;
                    latch.countDown();
                }
            });
        }
        latch.await();
        log.info(String.valueOf(syncTotal));
        log.info("消耗：" + stopWatch.getTime() + "ms");
    }
}

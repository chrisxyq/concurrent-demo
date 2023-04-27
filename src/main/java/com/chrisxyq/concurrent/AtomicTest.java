package com.chrisxyq.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存导致的可见性问题：volatile
 * 线程切换带来的原子性问题:加锁、原子类
 * 解决原子性问题，是要保证中间状态对外不可见
 * addOne 的例子中，set(get()+1)
 */
@Slf4j
public class AtomicTest {
    private static long       count      = 0;
    private static AtomicLong atomicLong = new AtomicLong(0);

    private void add10K() {
        int idx = 0;
        while (idx++ < 10000) {
            count += 1;
        }
    }

    /**
     * 对一个锁解锁 Happens-Before 后续对这个锁的加锁
     * 用了synchronized，解决了原子性问题，同时也解决了可见性问题
     * volatile只能解决可见性问题，不能解决原子性问题
     */
    private void add10KSynchronized() {
        synchronized (this) {
            int idx = 0;
            while (idx++ < 10000) {
                count += 1;
            }
        }
    }

    private void add10KAtomic() {
        int idx = 0;
        while (idx++ < 10000) {
            atomicLong.getAndIncrement();
        }
    }

    @Test
    public void test() throws InterruptedException {
        final AtomicTest test = new AtomicTest();
        // 创建两个线程，执行 add() 操作
        Thread th1 = new Thread(() -> {
            test.add10K();
        });
        Thread th2 = new Thread(() -> {
            test.add10K();
        });
        // 启动两个线程
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        th1.join();
        th2.join();
        log.info(String.valueOf(count));
    }

    @Test
    public void testVolatile() throws InterruptedException {
        final AtomicTest test = new AtomicTest();
        // 创建两个线程，执行 add() 操作
        Thread th1 = new Thread(() -> {
            test.add10KSynchronized();
        });
        Thread th2 = new Thread(() -> {
            test.add10KSynchronized();
        });
        // 启动两个线程
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        th1.join();
        th2.join();
        log.info(String.valueOf(count));
    }

    @Test
    public void testAtomic() throws InterruptedException {
        final AtomicTest test = new AtomicTest();
        // 创建两个线程，执行 add() 操作
        Thread th1 = new Thread(() -> {
            test.add10KAtomic();
        });
        Thread th2 = new Thread(() -> {
            test.add10KAtomic();
        });
        // 启动两个线程
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        th1.join();
        th2.join();
        log.info(String.valueOf(atomicLong.get()));
    }
}

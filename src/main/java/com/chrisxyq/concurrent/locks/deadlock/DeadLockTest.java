package com.chrisxyq.concurrent.locks.deadlock;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class DeadLockTest {
    /**
     * 提交到相同线程池中的任务一定是相互独立的，否则就一定要慎重。
     * 这种问题通用的解决方案是为不同的任务创建不同的线程池。
     * @throws InterruptedException
     */
    @Test
    public void test() throws InterruptedException {
        //L1、L2 阶段共用的线程池
        ExecutorService es = Executors.newFixedThreadPool(2);
        //L1 阶段的闭锁
        CountDownLatch l1 = new CountDownLatch(2);
        for (int i = 0; i < 2; i++) {
            log.info("L1,current thread:{}", Thread.currentThread().getName());
            // 执行 L1 阶段任务
            es.execute(() -> {
                //L2 阶段的闭锁
                CountDownLatch l2 = new CountDownLatch(2);
                // 执行 L2 阶段子任务
                for (int j = 0; j < 2; j++) {
                    es.execute(() -> {
                        log.info("L2,current thread:{}", Thread.currentThread().getName());
                        l2.countDown();
                    });
                }
                // 等待 L2 阶段任务执行完
                try {
                    l2.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                l1.countDown();
            });
        }
        // 等着 L1 阶段任务执行完
        l1.await();
        System.out.println("end");
    }

    /**
     * 会死锁
     * @throws InterruptedException
     */
    @Test
    public void test1() throws InterruptedException {
        ExecutorService pool = Executors
                .newSingleThreadExecutor();
        pool.submit(() -> {
            try {
                String qq=pool.submit(()->"QQ").get();
                System.out.println(qq);
            } catch (Exception e) {
            }
        });
        Thread.sleep(10000);
    }
}

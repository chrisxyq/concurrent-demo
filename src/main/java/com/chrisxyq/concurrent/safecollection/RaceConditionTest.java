package com.chrisxyq.concurrent.safecollection;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Vector;

/**
 * 两个或者以上进程或者线程并发执行时，其最终的结果依赖于进程或者线程执行的精确时序
 */
@Slf4j
public class RaceConditionTest {
    private static Vector<Integer> vector = new Vector<Integer>();

    /**
     * contains和add之间不是原子操作，有可能重复添加。
     *
     * @param o
     * @throws InterruptedException
     */
    public void addIfNotExist(Integer o) throws InterruptedException {
        Thread.sleep(2000);
        if (!vector.contains(o)) {
            vector.add(o);
        }
    }

    @Test
    public void test() throws InterruptedException {
        RaceConditionTest instance = new RaceConditionTest();
        Thread th1 = new Thread(() -> {
            try {
                instance.addIfNotExist(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread th2 = new Thread(() -> {
            try {
                instance.addIfNotExist(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        // 启动两个线程
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        th1.join();
        th2.join();
        log.info("vector:{}", vector);

    }
}

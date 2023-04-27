package com.chrisxyq.concurrent;

import org.junit.Test;

public class SafeCalc {
    long value = 0L;

    /**
     * 执行 addOne() 方法后，value 的值对 get() 方法是可见的吗？这个可见性是没法保证的。
     * 管程中锁的规则，是只保证后续对这个锁的加锁的可见性，而 get() 方法并没有加锁操作，
     * 所以可见性没法保证。那如何解决呢？很简单，就是 get() 方法也 synchronized 一下
     *
     * @return
     */
    long get() {
        System.out.println(String.format("get:%s", value));
        return value;
    }

    synchronized void addOne() {
        value += 1;
        System.out.println(String.format("addOne:%s", value));
    }

    @Test
    public void test() throws InterruptedException {
        // 创建两个线程，执行 add() 操作
        Thread th1 = new Thread(() -> {
            this.addOne();
        });
        Thread th2 = new Thread(() -> {
            this.get();
        });
        // 启动两个线程
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        th1.join();
        th2.join();
    }
}

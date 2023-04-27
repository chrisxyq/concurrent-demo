package com.chrisxyq.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class LifeCycleTest {
    public synchronized void umm1(Thread thread) throws InterruptedException {
        //RUNNABLE 转换到 BLOCKED
        //只有一种场景会触发这种转换，就是线程等待 synchronized 的隐式锁。
        log.info("thread.getState():{},thread:{}", thread.getState(),thread.getName());
        log.info("thread.getState():{},thread:{}",
                Thread.currentThread().getState(), Thread.currentThread().getName());
    }
    public synchronized void umm2() throws InterruptedException {
        log.info("thread.getState():{},thread:{}",
                Thread.currentThread().getState(), Thread.currentThread().getName());
    }
    @Test
    public void test() throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                umm2();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        log.info("thread.getState():{},thread:{}", thread.getState(),thread.getName());
        // 从 NEW 状态转换到 RUNNABLE 状态
        thread.start();
        log.info("thread.getState():{},thread:{}", thread.getState(),thread.getName());
        umm1(thread);


    }
}

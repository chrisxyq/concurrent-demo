package com.chrisxyq.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class HappensBeforeTest {
    static          int     x;
    volatile static boolean v = false;
    //
    static          int     var;

    public void writer() {
        x = 42;
        v = true;
    }

    public void reader() {
        if (v == true) {
            // 这里 x 会是多少呢？
            log.info(String.valueOf(x));
        }
    }

    /**
     * 1.“x=42” Happens-Before 写变量 “v=true” ，这是规则 1 的内容；
     * 2.写变量“v=true” Happens-Before 读变量 “v=true”，这是规则 2 的内容 。
     *
     * @throws InterruptedException
     */
    @Test
    public void test() throws InterruptedException {
        HappensBeforeTest instance = new HappensBeforeTest();
        Thread th1 = new Thread(() -> {
            instance.writer();
        });
        Thread th2 = new Thread(() -> {
            instance.reader();
        });
        // 启动两个线程
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        th1.join();
        th2.join();
    }

    /**
     * 1.如果线程 A 调用线程 B 的 start() 方法（即在线程 A 中启动线程 B），
     * 那么该 start() 操作 Happens-Before 于线程 B 中的任意操作
     * 2.如果在线程 A 中，调用线程 B 的 join() 并成功返回，
     * 那么线程 B 中的任意操作 Happens-Before 于该 join() 操作的返回。
     *
     * @throws InterruptedException
     */
    @Test
    public void test1() throws InterruptedException {
        Thread B = new Thread(() -> {
            // 主线程调用 B.start() 之前
            // 所有对共享变量的修改，此处皆可见
            // 此例中，var==77
            log.info("current thread:{},var:{}", Thread.currentThread().getName(), var);
            var = 66;
        });
        // 此处对共享变量 var 修改
        var = 77;
        // 主线程启动子线程
        B.start();
        B.join();
        log.info("current thread:{},var:{}", Thread.currentThread().getName(), var);
    }
}

package com.chrisxyq.concurrent;

import org.junit.Test;

/**
 * @author chrisxu
 * @create 2021-10-03 17:48
 * Ctrl + Alt + L：格式化代码
 * ctrl + Alt + T：代码块包围
 * ctrl + Y：删除行
 * ctrl + D：复制行
 * alt+上/下：移动光标到上/下方法
 * ctrl+shift+/：注释多行
 */
public class DemonThreadTest {

    @Test
    public void test() {
        Thread aa = new Thread(() -> {
            while (true) {
                System.out.println(Thread.currentThread().getName()
                        + "::" + Thread.currentThread().getState());
            }
        }, "demon");
        aa.start();
        System.out.println(Thread.currentThread().getName() + "结束");
    }

    /**
     * 设置为守护线程之后，主程序结束，守护线程也结束
     */
    @Test
    public void test1() {
        Thread aa = new Thread(() -> {
            while (true) {
                System.out.println(Thread.currentThread().getName()
                        + "::" + Thread.currentThread().getState());
            }
        }, "demon");
        //设置为守护线程之后，程序才会结束
        aa.setDaemon(true);
        aa.start();
        System.out.println(Thread.currentThread().getName() + "结束");
    }
}

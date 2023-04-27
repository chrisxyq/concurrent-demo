package com.chrisxyq.concurrent.locks;

/**
 * @author chrisxu
 * @create 2021-10-07 9:46
 * Ctrl + Alt + L：格式化代码
 * ctrl + Alt + T：代码块包围
 * ctrl + Y：删除行
 * ctrl + D：复制行
 * alt+上/下：移动光标到上/下方法
 * ctrl+shift+/：注释多行
 */
public class ReentrantLockDemo {
    public static void main(String[] args) {
        Object o = new Object();
        new Thread(()->{
            synchronized (o){
                System.out.println(Thread.currentThread().getName() + "外层");
                synchronized (o){
                    System.out.println(Thread.currentThread().getName() + "外层");
                    synchronized (o){
                        System.out.println(Thread.currentThread().getName() + "外层");
                    }
                }
            }
        });
    }
}

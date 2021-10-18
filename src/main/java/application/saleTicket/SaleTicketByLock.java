package application.saleTicket;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 题目：三个售票员   卖出   30张票
 * 笔记：如何编写企业级的多线程
 * 固定的编程套路+模板
 * 1.在高内聚低耦合的前提下，线程    操作(对外暴露的调用方法)     资源类
 * 1.1先创建一个资源类
 */
public class SaleTicketByLock {
    //主线程，一切程序的入口
    public static void main(String[] args) {
        LockTicket ticket = new LockTicket();
        //新版写法
        new Thread(() -> {
            for (int i = 1; i <= 40; i++) {
                ticket.sale();
            }
        }, "A").start();
        new Thread(() -> {
            for (int i = 1; i <= 40; i++) {
                ticket.sale();
            }
        }, "B").start();
        new Thread(() -> {
            for (int i = 1; i <= 40; i++) {
                ticket.sale();
            }
        }, "C").start();
    }
}
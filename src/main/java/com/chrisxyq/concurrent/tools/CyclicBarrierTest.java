package com.chrisxyq.concurrent.tools;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @create 2020/7/1 15:17
 */
@Slf4j
public class CyclicBarrierTest {
    // 订单队列
    Vector<P> pos;
    // 派送单队列
    Vector<D> dos;
    // 执行回调的线程池
    Executor  executor = Executors.newFixedThreadPool(1);
    final CyclicBarrier barrier =
            new CyclicBarrier(2, () -> {
                executor.execute(() -> check());
            });

    void check() {
        P p = pos.remove(0);
        D d = dos.remove(0);
        // 执行对账操作
        Diff diff = checkPD(p, d);
        // 差异写入差异库
        save(diff);
    }

    private void save(Diff diff) {
    }

    private Diff checkPD(P p, D d) {
        return null;
    }

    @Test
    public void test() {
        // 循环查询订单库
        Thread T1 = new Thread(() -> {
            //存在未对账订单
            while (existsP()) {
                // 查询订单库
                pos.add(getPOrders());
                // 等待
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        });
        T1.start();
        // 循环查询运单库
        Thread T2 = new Thread(() -> {
            //存在未对账订单
            while (existsD()) {
                // 查询运单库
                dos.add(getDOrders());
                // 等待
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        });
        T2.start();
    }

    private boolean existsP() {
        return true;
    }

    private P getPOrders() {
        return null;
    }

    private D getDOrders() {
        return null;
    }

    private boolean existsD() {
        return true;
    }

    @Test
    public void test1() throws InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(7, () -> {
            log.info("召唤神龙");
        });
        for (int i = 1; i <= 7; i++) {
            new Thread(() -> {
                log.info(Thread.currentThread().getName() + "星龙被收集到了\t");
                try {
                    cyclicBarrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, String.valueOf(i)).start();
        }
        Thread.sleep(4000);
    }
}

class P {

}

class D {

}

class Diff {

}
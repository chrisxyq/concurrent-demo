package com.chrisxyq.concurrent.threadpool.byhand;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

/**
 * 线程池和普通的池化资源有很大不同，线程池实际上是生产者 - 消费者模式的一种实现，
 * 理解生产者 - 消费者模式是理解线程池的关键所在。
 *
 * 线程池的设计，没有办法直接采用一般意义上池化资源的设计方法。
 * 那线程池该如何设计呢？目前业界线程池的设计，
 * 普遍采用的都是生产者 - 消费者模式。线程池的使用方是生产者，线程池本身是消费者。
 */
@Slf4j
class ThreadPoolByHand {
    /**
     * 利用阻塞队列实现生产者 - 消费者模式
     */
    BlockingQueue<Runnable> workQueue;
    /**
     * 保存内部工作线程
     */
    List<WorkerThread>      threads = new ArrayList<>();

    /**
     * 构造方法
     *
     * @param poolSize:工作线程的个数
     * @param workQueue:任务队列
     */
    ThreadPoolByHand(int poolSize,
                     BlockingQueue<Runnable> workQueue) {
        this.workQueue = workQueue;
        // 创建工作线程，并启动工作线程，开始取队列的任务进行消费
        for (int i = 0; i < poolSize; i++) {
            WorkerThread work = new WorkerThread();
            work.start();
            threads.add(work);
        }
    }

    /**
     * 提交任务
     * 将Runnable任务放到任务队列里
     * 由工作线程取了消费
     *
     * @param task
     */
    void execute(Runnable task) {
        try {
            workQueue.put(task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 工作线程负责消费任务，并执行任务
     * 循环取任务并执行
     */
    class WorkerThread extends Thread {
        public void run() {
            while (true) {
                Runnable task = null;
                try {
                    task = workQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

@Slf4j
public class ThreadPoolByHandTest {
    /**
     * 使用示例
     * 在 MyThreadPool 的内部，我们维护了一个阻塞队列 workQueue 和一组工作线程，
     * 工作线程的个数由构造函数中的 poolSize 来指定。
     * 用户通过调用 execute() 方法来提交 Runnable 任务，
     * execute() 方法的内部实现仅仅是将任务加入到 workQueue 中。
     * MyThreadPool 内部维护的工作线程会消费 workQueue 中的任务并执行任务
     */
    @Test
    public void test() {
        // 创建有界阻塞队列
        BlockingQueue<Runnable> workQueue =
                new LinkedBlockingQueue<>(2);
        // 创建线程池
        ThreadPoolByHand pool = new ThreadPoolByHand(
                2, workQueue);
        // 提交任务
        IntStream.rangeClosed(1, 5).forEach(i -> {
            pool.execute(() -> {
                log.info("thread:{},executing task:{}",
                        Thread.currentThread().getName(), i);
            });
        });
    }
}

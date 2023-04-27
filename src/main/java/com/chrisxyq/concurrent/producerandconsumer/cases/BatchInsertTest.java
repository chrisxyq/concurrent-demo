package com.chrisxyq.concurrent.producerandconsumer.cases;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 我们提到一个监控系统动态采集的案例，
 * 其实最终回传的监控数据还是要存入数据库的（如下图）。
 * 利用生产者 - 消费者模式实现批量执行 SQL 非常简单：
 * 将原来直接 INSERT 数据到数据库的线程作为生产者线程，
 * 生产者线程只需将数据添加到任务队列，
 * 然后消费者线程负责将任务从任务队列中批量取出并批量执行。
 */
@Slf4j
public class BatchInsertTest {
    // 任务队列
    BlockingQueue<Task> bq = new LinkedBlockingQueue<>(2000);
    ExecutorService     es = Executors.newFixedThreadPool(5);

    /**
     * 启动 5 个消费者线程
     * 执行批量任务
     */
    @Test
    public void test() {
        for (int i = 0; i < 5; i++) {
            es.execute(() -> {
                try {
                    while (true) {
                        // 获取批量任务
                        List<Task> ts = pollTasks();
                        // 执行批量任务
                        execTasks(ts);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 从任务队列中获取批量任务
     * 首先是以阻塞方式获取任务队列中的一条任务，而后则是以非阻塞的方式获取任务；
     * 之所以首先采用阻塞方式，是因为如果任务队列中没有任务，这样的方式能够避免无谓的循环。
     *
     * @return
     * @throws InterruptedException
     */
    List<Task> pollTasks() throws InterruptedException {
        List<Task> ts = new LinkedList<>();
        // 阻塞式获取一条任务
        Task t = bq.take();
        while (t != null) {
            ts.add(t);
            // 非阻塞式获取一条任务
            t = bq.poll();
        }
        return ts;
    }

    /**
     * 批量执行任务
     *
     * @param ts
     */
    void execTasks(List<Task> ts) {
        // 省略具体代码无数
    }

}

class Task {

}
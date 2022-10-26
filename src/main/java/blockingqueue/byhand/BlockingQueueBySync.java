package blockingqueue.byhand;

import lombok.extern.slf4j.Slf4j;
import utils.JsonUtils;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 使用 wait、notifyAll 实现阻塞队列
 */
@Slf4j
public class BlockingQueueBySync<T> implements IQueue<T> {
    private          Queue<T> queue = new LinkedList<T>();
    private volatile int      capacity;

    public BlockingQueueBySync(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public synchronized void put(T t) throws InterruptedException {
        // 队列满，阻塞
        while (capacity == this.queue.size()) {
            // 等待队列不满
            log.info("queue.put(i): i:{},BlockedQueue is full,put(T t) waiting...", t);
            this.wait();
        }
        // 入队操作...
        this.queue.offer(t);
        // 入队后, 通知可出队
        this.notifyAll();
        log.info("queue.put(i): i:{},queue:{},current thread:{}",
                t, JsonUtils.toJson(queue), Thread.currentThread().getName());
    }

    @Override
    public synchronized T take() throws InterruptedException {
        // 队列空，阻塞
        while (isEmpty()) {
            // 等待队列不空
            this.wait();
            log.info("BlockingQueueBySync is empty,take() waiting...");
        }
        // 出队操作...
        T element = this.queue.poll();
        // 出队后，通知可入队
        this.notifyAll();
        log.info("queue.take(): queue:{},queue.size():{},current thread:{}",
                JsonUtils.toJson(queue), this.size(), Thread.currentThread().getName());
        return element;
    }

    @Override
    public synchronized int size() {
        return queue.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return queue.size() == 0;
    }
}

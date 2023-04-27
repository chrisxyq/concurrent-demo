package com.chrisxyq.concurrent;


import lombok.extern.slf4j.Slf4j;
import com.chrisxyq.concurrent.utils.JsonUtils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用await、signal实现阻塞队列
 */
@Slf4j
public class BlockingQueueByLock<T> implements IQueue<T> {
    private          Queue<T>  queue    = new LinkedList<T>();
    private volatile int       capacity;
    final            Lock      lock     = new ReentrantLock();
    /**
     * 条件变量：队列不满
     */
    final            Condition notFull  = lock.newCondition();
    /**
     * 条件变量：队列不空
     */
    final            Condition notEmpty = lock.newCondition();

    public BlockingQueueByLock(int capacity) {
        this.capacity = capacity;
    }


    /**
     * 入队
     *
     * @param t
     */
    public void put(T t) throws InterruptedException {
        lock.lock();
        try {
            // 队列满，阻塞
            while (capacity == this.queue.size()) {
                // 等待队列不满
                log.info("queue.put(i): i:{},BlockedQueue is full,put(T t) waiting...", t);
                notFull.await();
            }
            // 入队操作...
            this.queue.offer(t);
            // 入队后, 通知可出队
            notEmpty.signal();
        } finally {
            log.info("queue.put(i): i:{},queue:{},current thread:{}",
                    t, JsonUtils.toJson(queue), Thread.currentThread().getName());
            lock.unlock();
        }
    }

    /**
     * 出队
     */
    public T take() throws InterruptedException {
        lock.lock();
        try {
            // 队列空，阻塞
            while (isEmpty()) {
                // 等待队列不空
                notEmpty.await();
                log.info("BlockingQueueByLock is empty,take() waiting...");
            }
            // 出队操作...
            T element = this.queue.poll();
            // 出队后，通知可入队
            notFull.signal();
            return element;
        } finally {
            log.info("queue.take(): queue:{},queue.size():{},current thread:{}",
                    JsonUtils.toJson(queue), this.size(), Thread.currentThread().getName());
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.lock();
        try {
            return queue.size() == 0;
        } finally {
            lock.unlock();
        }

    }
}
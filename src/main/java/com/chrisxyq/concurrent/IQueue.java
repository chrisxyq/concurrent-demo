package com.chrisxyq.concurrent;

public interface IQueue<E> {
    /**
     * 添加新元素，当队列满则阻塞
     * @param e
     * @throws InterruptedException
     */
    void put(E e) throws InterruptedException;

    /**
     * 弹出队头元素，当队列空则阻塞
     * @return
     * @throws InterruptedException
     */
    E take() throws InterruptedException;

    /**
     * 队列元素个数
     * @return
     */
    int size();

    /**
     * 队列是否为空
     * @return
     */
    boolean isEmpty();

}

package com.chrisxyq.concurrent.avoidshare.copyonwrite;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Copy-on-Write 模式和线程本地存储模式本质上都是为了避免共享
 * 解决方案2：CopyOnWrite
 * CopyOnWrite，顾名思义就是写的时候会将共享变量新复制一份出来，这样做的好处是读操作完全无锁。
 * 读写是可以并行的，遍历操作一直都是基于原 array 执行，而写操作则是基于新 array 进行。
 *
 * Copy-on-Write 才是最简单的并发解决方案。它是如此简单，
 * 以至于 Java 中的基本数据类型 String、Integer、Long 等都是基于 Copy-on-Write 方案实现的
 * <p>
 * 使用 CopyOnWriteArrayList 需要注意的“坑”主要有两个方面。
 * 一个是应用场景，
 * CopyOnWriteArrayList 仅适用于写操作非常少的场景，而且能够容忍读写的短暂不一致。
 * 例如上面的例子中，写入的新元素并不能立刻被遍历到。
 * <p>
 * 另一个需要注意的是，CopyOnWriteArrayList 迭代器是只读的，不支持增删改。
 * UnsupportedOperationException
 * 因为迭代器遍历的仅仅是一个快照，而对快照进行增删改是没有意义的。
 *
 * Java 提供了 CopyOnWriteArrayList，为什么没有提供 CopyOnWriteLinkedList 呢？
 * CopyOnWriteLinkedList的链表结构读取效率比较低，就违背了读多写少的设计初衷
 *
 * @throws InterruptedException
 */
@Slf4j
public class CopyOnWriteTest {
    private void unsafeIterate(List<Integer> list) {
        Iterator<Integer> i = list.iterator();
        while (i.hasNext()) {
            for (int j = 0; j < 100; j++) {
                log.info("i.next():{}", i.next());
            }
            i.remove();
        }
    }

    @Test
    public void testCopyOnWriteIterate() throws InterruptedException {
        List<Integer> list = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        new Thread(() -> {
            unsafeIterate(list);
        }, "A").start();
        new Thread(() -> {
            list.remove(4);
        }, "B").start();
        Thread.sleep(5000);
    }
}

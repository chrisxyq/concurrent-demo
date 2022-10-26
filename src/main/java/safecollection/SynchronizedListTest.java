package safecollection;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class SynchronizedListTest {

    private void unsafeIterate(List<Integer> list) {
        Iterator<Integer> i = list.iterator();
        while (i.hasNext()) {
            for (int j = 0; j < 100; j++) {
                log.info("i.next():{}", i.next());
            }
            i.remove();
        }
    }

    private void safeIterate(List<Integer> list) {
        synchronized (list) {
            Iterator<Integer> i = list.iterator();
            while (i.hasNext()) {
                for (int j = 0; j < 100; j++) {
                    log.info("i.next():{}", i.next());
                }
            }
        }
    }

    /**
     * ConcurrentModificationException
     *
     * @throws InterruptedException
     */
    @Test
    public void test() throws InterruptedException {
        //        List<String> list = new CopyOnWriteArrayList<>();
//        List<String> list = Collections.synchronizedList(new ArrayList<>());
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            new Thread(() -> {
                list.add(UUID.randomUUID().toString().substring(0, 8));
                System.out.println(list);
            }, String.valueOf(i)).start();
        }
    }

    /**
     * 线程不安全的迭代器Iterator
     * 即使是线程安全的集合，在使用迭代器进行迭代时，也要加锁
     * ConcurrentModificationException
     *
     * @throws InterruptedException
     */
    @Test
    public void testUnsafeIterate() throws InterruptedException {
        List<Integer> list = Collections.synchronizedList(new ArrayList<>());
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

    /**
     * 解决方案1：使用synchronized给迭代器加锁
     * @throws InterruptedException
     */
    @Test
    public void testSafeIterate() throws InterruptedException {
        List<Integer> list = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        new Thread(() -> {
            safeIterate(list);
        }, "A").start();
        new Thread(() -> {
            list.remove(4);
        }, "B").start();
        Thread.sleep(5000);
    }

    /**
     * 解决方案2：CopyOnWrite
     * CopyOnWrite，顾名思义就是写的时候会将共享变量新复制一份出来，这样做的好处是读操作完全无锁。
     * 读写是可以并行的，遍历操作一直都是基于原 array 执行，而写操作则是基于新 array 进行。
     *
     * 使用 CopyOnWriteArrayList 需要注意的“坑”主要有两个方面。
     * 一个是应用场景，
     * CopyOnWriteArrayList 仅适用于写操作非常少的场景，而且能够容忍读写的短暂不一致。
     * 例如上面的例子中，写入的新元素并不能立刻被遍历到。
     *
     * 另一个需要注意的是，CopyOnWriteArrayList 迭代器是只读的，不支持增删改。
     * UnsupportedOperationException
     * 因为迭代器遍历的仅仅是一个快照，而对快照进行增删改是没有意义的。
     * @throws InterruptedException
     */
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

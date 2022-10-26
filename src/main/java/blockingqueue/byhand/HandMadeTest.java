package blockingqueue.byhand;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class HandMadeTest {
    @Test
    public void test() throws InterruptedException {
        BlockingQueueByLock<Integer> queue = new BlockingQueueByLock<>(5);
        Thread th1 = new Thread(() -> {
            for (int i = 0; i < 6; i++) {
                try {
                    queue.put(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        Thread th2 = new Thread(() -> {
            try {
                queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        // 从 NEW 状态转换到 RUNNABLE 状态
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        th1.join();
        th2.join();
    }

    @Test
    public void test1() throws InterruptedException {
        BlockingQueueBySync<Integer> queue = new BlockingQueueBySync<>(5);
        Thread th1 = new Thread(() -> {
            for (int i = 0; i < 6; i++) {
                try {
                    queue.put(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        Thread th2 = new Thread(() -> {
            try {
                queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        // 启动两个线程
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        th1.join();
        th2.join();
    }
}

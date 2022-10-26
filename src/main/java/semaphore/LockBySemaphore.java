package semaphore;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.Semaphore;

@Slf4j
public class LockBySemaphore {
    private static int       countBySemaphore;
    private static int       count;
    /**
     * 初始化信号量
     */
    private final  Semaphore s = new Semaphore(1);

    /**
     * 用信号量实现lock保证互斥
     *
     * @throws InterruptedException
     */
    private void addOneBySemaphore() throws InterruptedException {
        s.acquire();
        try {
            countBySemaphore += 1;
        } finally {
            s.release();
        }
    }

    private void addOne() throws InterruptedException {
        count += 1;
    }

    @Test
    public void test() throws InterruptedException {
        LockBySemaphore lockBySemaphore = new LockBySemaphore();
        for (int i = 1; i <= 10000; i++) {
            new Thread(() -> {
                try {
                    lockBySemaphore.addOneBySemaphore();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, String.valueOf(i)).start();
        }
        for (int i = 1; i <= 10000; i++) {
            new Thread(() -> {
                try {
                    lockBySemaphore.addOne();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, String.valueOf(i)).start();
        }
        //等待所有线程结束
        Thread.sleep(5000);
        log.info("lockBySemaphore.addOneBySemaphore() count:{}", this.countBySemaphore);
        log.info("lockBySemaphore.addOne() count:{}", this.count);
    }

}

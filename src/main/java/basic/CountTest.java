package basic;

import org.junit.Test;

public class CountTest {
    private volatile long count = 0;

    private void add10K() {
        int idx = 0;
        while (idx++ < 10000) {
            count += 1;
        }
    }

    /**
     * 这段代码有两个问题：
     * 1.cpu缓存导致的可见性问题
     * 两个线程都是基于 CPU 缓存里的 count 值来计算，所以导致最终 count 的值都是小于 20000 的。
     * 这就是缓存的可见性问题。
     * 2.线程切换带来的原子性问题
     * 操作系统做任务切换，可以发生在任何一条CPU 指令执行完，是 CPU 指令，而不是高级语言里的一条语句。
     * 我们需要在高级语言层面保证操作的原子性。
     * @throws InterruptedException
     */
    @Test
    public void calc() throws InterruptedException {
        // 创建两个线程，执行 add() 操作
        Thread th1 = new Thread(() -> {
            this.add10K();
        });
        Thread th2 = new Thread(() -> {
            this.add10K();
        });
        // 启动两个线程
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        th1.join();
        th2.join();
        System.out.println(count);
    }
}

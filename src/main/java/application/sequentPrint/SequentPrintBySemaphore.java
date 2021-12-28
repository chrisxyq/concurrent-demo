package application.sequentPrint;

import java.util.concurrent.Semaphore;

/**
 * @author yuanqixu
 */
public class SequentPrintBySemaphore {
    private int times;
    private static Semaphore semaphoreA = new Semaphore(1); // 只有A 初始信号量为1,第一次获取到的只能是A
    private static Semaphore semaphoreB = new Semaphore(0);
    private static Semaphore semaphoreC = new Semaphore(0);

    public SequentPrintBySemaphore(int times) {
        this.times = times;
    }

    public static void main(String[] args) {
        SequentPrintBySemaphore printer = new SequentPrintBySemaphore(1);
        new Thread(() -> {
            printer.print("A", semaphoreA, semaphoreB);
        }, "A").start();

        new Thread(() -> {
            printer.print("B", semaphoreB, semaphoreC);
        }, "B").start();

        new Thread(() -> {
            printer.print("C", semaphoreC, semaphoreA);
        }, "C").start();
    }

    private void print(String name, Semaphore current, Semaphore next) {
        for (int i = 0; i < times; i++) {
            try {
                System.out.println("111" + Thread.currentThread().getName());
                current.acquire();  // A获取信号执行,A信号量减1,当A为0时将无法继续获得该信号量
                System.out.print(name);
                next.release();    // B释放信号，B信号量加1（初始为0），此时可以获取B信号量
                System.out.println("222" + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

package application.sequentPrint;

import java.util.concurrent.locks.LockSupport;

/**
 * @author yuanqixu
 * 用两个线程，一个输出字母，一个输出数字，交替输出 1A2B3C4D...26Z
 * LockSupport 是 JDK 底层的基于 sun.misc.Unsafe 来实现的类，用来创建锁和其他同步工具类的基本线程阻塞原语。
 * 它的静态方法unpark()和park()可以分别实现阻塞当前线程和唤醒指定线程的效果，所以用它解决这样的问题会更容易一些。
 *
 * （在 AQS 中，就是通过调用 LockSupport.park( )和 LockSupport.unpark() 来实现线程的阻塞和唤醒的。）
 */
public class SequentPrintByLockSupport {
    private static Thread numThread, letterThread;

    public static void main(String[] args) {
        letterThread = new Thread(() -> {
            for (int i = 0; i < 26; i++) {
                System.out.print((char) ('A' + i));
                LockSupport.unpark(numThread);
                LockSupport.park();
            }
        }, "letterThread");

        numThread = new Thread(() -> {
            for (int i = 1; i <= 26; i++) {
                System.out.print(i);
                LockSupport.park();
                LockSupport.unpark(letterThread);
            }
        }, "numThread");
        numThread.start();
        letterThread.start();
    }
}

package cases.incranddecr;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chrisxu
 * @create 2021-10-05 0:13
 * Ctrl + Alt + L：格式化代码
 * ctrl + Alt + T：代码块包围
 * ctrl + Y：删除行
 * ctrl + D：复制行
 * alt+上/下：移动光标到上/下方法
 * ctrl+shift+/：注释多行
 */
public class ShareByLock {
    private int number = 0;
    private Lock lock = new ReentrantLock();
    private Condition condition =lock.newCondition();

    public void incr() throws InterruptedException {
        //上锁
        lock.lock();
        try{
            //等待
            while (number != 0) {
                condition.await();
            }
            //干活
            number++;
            System.out.println(Thread.currentThread().getName() + "::" + number);
            //通知其他线程
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    public void decr() throws InterruptedException {
        //上锁
        lock.lock();
        try{
            //等待
            while (number != 1) {
                condition.await();
            }
            //干活
            number--;
            System.out.println(Thread.currentThread().getName() + "::" + number);
            //通知其他线程
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }
}

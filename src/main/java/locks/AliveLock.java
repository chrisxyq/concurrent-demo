package locks;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import utils.JsonUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
class Account {
    private       int  balance;
    private final Lock lock = new ReentrantLock();

    public Account(int balance) {
        this.balance = balance;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    /**
     * synchronized 之所以能够保证可见性，也是因为有一条 synchronized 相关的规则：
     * synchronized 的解锁 Happens-Before 于后续对这个锁的加锁。
     * <p>
     * <p>
     * lock是怎么保证可见性的
     * 利用了 volatile 相关的 Happens-Before 规则。
     * Java SDK 里面的 ReentrantLock，
     * 内部持有一个 volatile 的成员变量 state，
     * 获取锁的时候，会读写 state 的值；解锁的时候，
     * 也会读写 state 的值（简化后的代码如下面所示）。
     * 也就是说，在执行 value+=1 之前，程序先读写了一次 volatile 变量 state，
     * 在执行 value+=1 之后，又读写了一次 volatile 变量 state。
     * <p>
     * 活锁
     *
     * @param tar
     * @param amt
     */
    void transferAliveLock(Account tar, int amt) {
        boolean successFlag = false;
        while (true) {
            if (this.lock.tryLock()) {
                log.info("get lock:{},current thread:{}",
                        this.lock, Thread.currentThread().getName());
                try {
                    if (tar.lock.tryLock()) {
                        log.info("get lock:{},current thread:{}",
                                tar.lock, Thread.currentThread().getName());
                        try {
                            this.balance -= amt;
                            tar.balance += amt;
                        } finally {
                            log.info("unlock lock:{},current thread:{}",
                                    tar.lock, Thread.currentThread().getName());
                            log.info("transfer success! this.Balance:{},tar.Balance:{}",
                                    this.getBalance(), tar.getBalance());
                            successFlag = true;
                            tar.lock.unlock();
                        }
                    }//if
                } finally {
                    log.info("unlock lock:{},current thread:{}",
                            this.lock, Thread.currentThread().getName());
                    this.lock.unlock();
                    if (successFlag) {
                        break;
                    }
                }
            }//if
        }//while
    }//transfer

    /**
     * 加上随机等待时间，避免活锁
     *
     * @param tar
     * @param amt
     * @throws InterruptedException
     */
    void transfer(Account tar, int amt) throws InterruptedException {
        boolean successFlag = false;
        while (true) {
            if (this.lock.tryLock(new Random(1000).nextInt(), TimeUnit.SECONDS)) {
                log.info("get lock:{},current thread:{}",
                        this.lock, Thread.currentThread().getName());
                try {
                    if (tar.lock.tryLock(new Random(1000).nextInt(), TimeUnit.SECONDS)) {
                        log.info("get lock:{},current thread:{}",
                                tar.lock, Thread.currentThread().getName());
                        try {
                            this.balance -= amt;
                            tar.balance += amt;
                        } finally {
                            log.info("unlock lock:{},current thread:{}",
                                    tar.lock, Thread.currentThread().getName());
                            log.info("transfer success! this.Balance:{},tar.Balance:{}",
                                    this.getBalance(), tar.getBalance());
                            successFlag = true;
                            tar.lock.unlock();
                        }
                    }//if
                } finally {
                    log.info("unlock lock:{},current thread:{}",
                            this.lock, Thread.currentThread().getName());
                    this.lock.unlock();
                    if (successFlag) {
                        break;
                    }
                }
            }//if
        }//while
    }//transfer
}

@Slf4j
public class AliveLock {
    @Test
    public void test() throws InterruptedException {
        Account me = new Account(100);
        Account tar = new Account(100);
        Thread th1 = new Thread(() -> {
            me.transferAliveLock(tar, 50);
        });
        Thread th2 = new Thread(() -> {
            tar.transferAliveLock(me, 50);
        });
        // 启动两个线程
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        th1.join();
        th2.join();
    }

    @Test
    public void test1() throws InterruptedException {
        Account me = new Account(100);
        Account tar = new Account(100);
        Thread th1 = new Thread(() -> {
            try {
                me.transfer(tar, 50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread th2 = new Thread(() -> {
            try {
                tar.transfer(me, 50);
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

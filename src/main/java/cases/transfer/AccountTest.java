package cases.transfer;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
class Account {
    /**
     * 锁资源收集器，一次获取所有锁
     */
    private Allocator actr;
    /**
     * 为锁资源添加id，有序获取锁
     */
    private int       id;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    /**
     * todo:写个demo说明 用“等待-通知”机制优化循环等待的效果 就医流程
     * 用“等待-通知”机制优化循环等待
     */
    static class AllocatorWaitNotify {
        private List<Object> als;

        /**
         * 一次性申请所有资源
         *
         * @param from
         * @param to
         */
        synchronized void apply(
                Object from, Object to) {
            // 经典写法:while(条件不满足) wait
            while (als.contains(from) ||
                    als.contains(to)) {
                try {
                    wait();
                } catch (Exception e) {
                    log.warn("AllocatorWaitNotify.apply", e);
                }
            }
            als.add(from);
            als.add(to);
        }

        /**
         * 归还资源
         * 假设我们有资源 A、B、C、D，线程 1 申请到了 AB，
         * 线程 2 申请到了 CD，此时线程 3 申请 AB，
         * 会进入等待队列（AB 分配给线程 1，线程 3 要求的条件不满足），
         * 线程 4 申请 CD 也会进入等待队列。我们再假设之后线程 1 归还了资源 AB，
         * 如果使用 notify() 来通知等待队列中的线程，有可能被通知的是线程 4，
         * 但线程 4 申请的是 CD，所以此时线程 4 还是会继续等待，
         * 而真正该唤醒的线程 3 就再也没有机会被唤醒了。
         *
         * 所以除非经过深思熟虑，否则尽量使用 notifyAll()。
         *
         * @param from
         * @param to
         */
        synchronized void free(
                Object from, Object to) {
            als.remove(from);
            als.remove(to);
            notifyAll();
        }
    }

    static class Allocator {
        private List<Object> als =
                new ArrayList<>();

        /**
         * 一次性申请所有资源
         *
         * @param from
         * @param to
         * @return
         */
        synchronized boolean apply(
                Object from, Object to) {
            if (als.contains(from) ||
                    als.contains(to)) {
                return false;
            } else {
                als.add(from);
                als.add(to);
            }
            return true;
        }

        /**
         * 归还资源
         *
         * @param from
         * @param to
         */
        synchronized void free(
                Object from, Object to) {
            als.remove(from);
            als.remove(to);
        }
    }

    private int balance;

    /**
     * 此方法未必线程安全
     * this 这把锁可以保护自己的余额 this.balance，却保护不了别人的余额 target.balance
     * 可能有其他方法/线程访问 target
     * target的余额未必受保护
     *
     * @param target
     * @param amt
     */
    synchronized void transfer(
            Account target, int amt) {
        if (this.balance > amt) {
            this.balance -= amt;
            target.balance += amt;
        }
    }

    /**
     * 解决方案1：
     * 使用共享的锁 Account.class 来保护不同对象的临界区
     * 缺点：串行化性能太差
     *
     * @param target
     * @param amt
     */
    void transfer1(Account target, int amt) {
        synchronized (Account.class) {
            if (this.balance > amt) {
                this.balance -= amt;
                target.balance += amt;
            }
        }
    }

    /**
     * 解决方案2：细粒度的锁
     * 优点：可以并行执行
     * 缺点：有死锁风险
     *
     * @param target
     * @param amt
     */
    void transfer2(Account target, int amt) throws InterruptedException {
        // 锁定转出账户
        synchronized (this) {
            // 锁定转入账户
            Thread.sleep(1000);
            log.info("get lock:{},waiting for lock:{}", this, target);
            synchronized (target) {
                if (this.balance > amt) {
                    this.balance -= amt;
                    target.balance += amt;
                }
            }
        }
    }

    /**
     * 解决死锁1
     * 一次性申请转出账户和转入账户，直到成功
     * <p>
     * 缺点：如果 apply() 操作耗时长，或者并发冲突量大的时候，
     * 循环等待这种方案就不适用了，因为在这种场景下，
     * 可能要循环上万次才能获取到锁，太消耗 CPU 了。
     * <p>
     * 最好的方案应该是：如果线程要求的条件（转出账本和转入账本同在文件架上）不满足，
     * 则线程阻塞自己，进入等待状态；当线程要求的条件（转出账本和转入账本同在文件架上）满足后，
     * 通知等待的线程重新执行。其中，使用线程阻塞的方式就能避免循环等待消耗 CPU 的问题。
     *
     * @param target
     * @param amt
     */
    void transfer3(Account target, int amt) {
        //自旋
        while (!actr.apply(this, target)) {

        }
        log.info("current thread:{},get all locks,start transfer",
                Thread.currentThread().getName());
        try {
            // 锁定转出账户
            synchronized (this) {
                // 锁定转入账户
                synchronized (target) {
                    if (this.balance > amt) {
                        this.balance -= amt;
                        target.balance += amt;
                    }
                }
            }
        } finally {
            actr.free(this, target);
        }
    }

    /**
     * 解决死锁2
     * 有序申请锁资源，防止死锁
     * <p>
     * 缺点：如果 apply() 操作耗时长，或者并发冲突量大的时候，
     * 循环等待这种方案就不适用了，因为在这种场景下，
     * 可能要循环上万次才能获取到锁，太消耗 CPU 了。
     * <p>
     * 最好的方案应该是：如果线程要求的条件（转出账本和转入账本同在文件架上）不满足，
     * 则线程阻塞自己，进入等待状态；当线程要求的条件（转出账本和转入账本同在文件架上）满足后，
     * 通知等待的线程重新执行。其中，使用线程阻塞的方式就能避免循环等待消耗 CPU 的问题。
     *
     * @param target
     * @param amt
     */
    void transfer4(Account target, int amt) {
        Account left = this;
        Account right = target;
        if (this.id > target.id) {
            left = target;
            right = this;
        }
        // 锁定序号小的账户
        synchronized (left) {
            // 锁定序号大的账户
            synchronized (right) {
                if (this.balance > amt) {
                    this.balance -= amt;
                    target.balance += amt;
                }
            }
        }
    }
}

@Slf4j
public class AccountTest {
    @Test
    public void test() {
        Account account = new Account(new Account.Allocator(), 1, 1000);
        Account target = new Account(new Account.Allocator(), 2, 1000);
        account.transfer(target, 100);
        log.info("account.balance:{},target balance:{}", account.getBalance(), target.getBalance());
    }

    @Test
    public void test1() {
        Account account = new Account(new Account.Allocator(), 1, 1000);
        Account target = new Account(new Account.Allocator(), 2, 1000);
        account.transfer1(target, 100);
        log.info("account.balance:{},target balance:{}", account.getBalance(), target.getBalance());
    }

    /**
     * 产生死锁的demo
     *
     * @throws InterruptedException
     */
    @Test
    public void test2() throws InterruptedException {
        Account account = new Account(new Account.Allocator(), 1, 1000);
        Account target = new Account(new Account.Allocator(), 2, 1000);
        Thread th1 = new Thread(() -> {
            try {
                account.transfer2(target, 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread th2 = new Thread(() -> {
            try {
                target.transfer2(account, 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        th1.start();
        th2.start();
        th1.join();
        th2.join();
        log.info("account.balance:{},target balance:{}", account.getBalance(), target.getBalance());
    }

    @Test
    public void test3() throws InterruptedException {
        Account account = new Account(new Account.Allocator(), 1, 1000);
        Account target = new Account(new Account.Allocator(), 2, 1000);
        Thread th1 = new Thread(() -> {
            account.transfer3(target, 100);
        });
        Thread th2 = new Thread(() -> {
            target.transfer3(account, 100);
        });
        th1.start();
        th2.start();
        th1.join();
        th2.join();
        log.info("account.balance:{},target balance:{}", account.getBalance(), target.getBalance());
    }

    @Test
    public void test4() {
        Account account = new Account(new Account.Allocator(), 1, 1000);
        Account target = new Account(new Account.Allocator(), 2, 1000);
        account.transfer4(target, 100);
        log.info("account.balance:{},target balance:{}", account.getBalance(), target.getBalance());
    }
}

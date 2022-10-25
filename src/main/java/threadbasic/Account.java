package threadbasic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Account {
    /**
     * 锁资源收集器，一次获取所有锁
     */
    private Allocator actr;
    /**
     * 为锁资源添加id，有序获取锁
     */
    private int       id;

    /**
     * 用“等待-通知”机制优化循环等待
     */
    class AllocatorWaitNotify {
        private List<Object> als;
        // 一次性申请所有资源
        synchronized void apply(
                Object from, Object to){
            // 经典写法
            while(als.contains(from) ||
                    als.contains(to)){
                try{
                    wait();
                }catch(Exception e){
                }
            }
            als.add(from);
            als.add(to);
        }
        // 归还资源
        synchronized void free(
                Object from, Object to){
            als.remove(from);
            als.remove(to);
            notifyAll();
        }
    }
    class Allocator {
        private List<Object> als =
                new ArrayList<>();

        // 一次性申请所有资源
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

        // 归还资源
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
    void transfer2(Account target, int amt) {
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
    }

    /**
     * 解决死锁1
     * 一次性申请转出账户和转入账户，直到成功
     *
     * @param target
     * @param amt
     */
    void transfer3(Account target, int amt) {
        //自旋
        while (!actr.apply(this, target)) ;
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
         ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
        map.put(1,1);
        map.get(1);
    }

    /**
     * 解决死锁2
     * 有序申请锁资源，防止死锁
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

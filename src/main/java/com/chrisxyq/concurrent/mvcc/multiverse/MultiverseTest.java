package com.chrisxyq.concurrent.mvcc.multiverse;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.multiverse.api.StmUtils;
import org.multiverse.api.references.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.multiverse.api.StmUtils.*;

/**
 * 编程语言都有从数据库的事务管理中获得灵感，并且总结出了一个新的并发解决方案：
 * 软件事务内存（Software Transactional Memory，简称 STM）。
 * 传统的数据库事务，支持 4 个特性：原子性（Atomicity）、一致性（Consistency）、
 * 隔离性（Isolation）和持久性（Durability），
 * 也就是大家常说的 ACID，STM 由于不涉及到持久化，所以只支持 ACI。
 * <p>
 * MVCC 可以简单地理解为数据库事务在开启的时候，会给数据库打一个快照，
 * 以后所有的读写都是基于这个快照的。当提交事务的时候，
 * 如果所有读写过的数据在该事务执行期间没有发生过变化，
 * 那么就可以提交；如果发生了变化，说明该事务和有其他事务读写的数据冲突了，
 * 这个时候是不可以提交的。
 */
@Slf4j
class Account {
    // 余额
    private TxnLong balance;

    public TxnLong getBalance() {
        return balance;
    }

    // 构造函数
    public Account(long balance) {
        this.balance = StmUtils.newTxnLong(balance);
    }


    /**
     * 转账
     *
     * @param to
     * @param amt
     */
    public void transfer(Account to, int amt) {
        // 原子化操作
        atomic(() -> {
            log.info("transfering to:{} start", to.hashCode());
            if (this.balance.get() > amt) {
                this.balance.decrement(amt);
                to.balance.increment(amt);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("transfering to:{} end", to.hashCode());
        });
    }
}

@Slf4j
public class MultiverseTest {
    ExecutorService threadPool = Executors.newFixedThreadPool(2);

    @Test
    public void test() throws ExecutionException, InterruptedException {
        Account a = new Account(100);
        Account b = new Account(100);
        Future<?> futureA = threadPool.submit(() -> {
            a.transfer(b, 50);
        });
        Future<?> futureB = threadPool.submit(() -> {
            b.transfer(a, 50);
        });
        futureA.get();
        futureB.get();
        log.info("a.balance:{},b balance:{}",
                a.getBalance().atomicGet(), b.getBalance().atomicGet());
    }
}

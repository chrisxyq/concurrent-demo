package mvcc.byhand;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class TransferTest {
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
        STM.atomic((txn) -> {
            log.info("a.balance:{},b balance:{}",
                    a.getBalance().getValue(txn), b.getBalance().getValue(txn));
        });
    }
}

@Slf4j
class Account {
    // 余额
    private TxnRef<Integer> balance;

    public TxnRef<Integer> getBalance() {
        return balance;
    }

    // 构造方法
    public Account(int balance) {
        this.balance = new TxnRef<Integer>(balance);
    }

    // 转账操作
    public void transfer(Account target, int amt) {
        STM.atomic((txn) -> {
            Integer from = balance.getValue(txn);
            balance.setValue(from - amt, txn);
            Integer to = target.balance.getValue(txn);
            target.balance.setValue(to + amt, txn);
        });
    }
}
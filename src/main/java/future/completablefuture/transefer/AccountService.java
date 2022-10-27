package future.completablefuture.transefer;

import java.util.concurrent.CompletableFuture;

public class AccountService {
    /**
     * 变更账户金额
     *
     * @param account 账户 ID
     * @param amount  增加的金额，负值为减少
     */
    public CompletableFuture<Void> add(int account, int amount) throws InterruptedException {
        return CompletableFuture.runAsync(() -> {
            //长时间的计算任务
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(String.format("add,current thread:%s",Thread.currentThread().getName()));
        });
    }
}

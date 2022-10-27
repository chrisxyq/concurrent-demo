package future.completablefuture.transefer;

import java.util.concurrent.CompletableFuture;

public class TransferService {
    private AccountService accountService = new AccountService(); // 使用依赖注入获取账户服务的实例

    public CompletableFuture<Void> transfer(int fromAccount, int toAccount, int amount) throws InterruptedException {
        // 异步调用 add 方法从 fromAccount 扣减相应金额
        return accountService.add(fromAccount, -1 * amount)
                // 然后调用 add 方法给 toAccount 增加相应金额
                .thenCompose(v -> {
                    try {
                        return accountService.add(toAccount, amount);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }
}

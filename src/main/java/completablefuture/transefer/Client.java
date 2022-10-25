package completablefuture.transefer;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Client {
    private              TransferService transferService = new TransferService(); // 使用依赖注入获取转账服务的实例
    private final static int                                         A               = 1000;
    private final static int             B               = 1001;

    @Test
    public void syncInvoke() throws ExecutionException, InterruptedException {
        // 同步调用
        CompletableFuture<Void> future = transferService.transfer(A, B, 100);
        future.get();
        System.out.println(String.format("转账完成！,current thread:%s", Thread.currentThread().getName()));
    }
    @Test
    public void asyncInvoke() throws InterruptedException {
        // 异步调用
        //如果你执行第一个任务的时候，传入了一个线程池，当执行第二个任务的时候调用的是thenRun方法，则第二个任务和第一个任务是公用同一个线程池。
        transferService.transfer(A, B, 100)
                .thenRun(() ->
                        System.out.println(String.format("转账完成！,current thread:%s", Thread.currentThread().getName())));
        //Thread.sleep(10000);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new Client().asyncInvoke();
    }
}

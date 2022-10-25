package completablefuture;

import java.util.concurrent.CompletableFuture;

/**
 * @author yuanqixu
 */
public class CompletableFutureDemo {
    public static void main(String[] args) throws Exception {
        //无返回值的异步回调CompletableFuture.runAsync
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "没有返回值，update mysql ok");
        });
        completableFuture.get();

        //有返回值的异步回调CompletableFuture.supplyAsync
        CompletableFuture<Integer> completableFuture2 = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "有返回值，insert mysql ok");
            int age = 10 / 0;
            return 1024;
        });
        //t为返回值，u为异常信息
        completableFuture2.whenComplete((t,u)->{
            System.out.println("*****t:"+t);
            System.out.println("*****u:"+u);
        }).exceptionally(f->{
            System.out.println("*****exception:"+f.getMessage());
            return 4444;
        }).get();
    }
}

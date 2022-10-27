package future;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * 对于简单的并行任务，你可以通过“线程池 +Future”的方案来解决
 * Future 可以类比为现实世界里的提货单，比如去蛋糕店订生日蛋糕，蛋糕店都是先给你一张提货单，
 * 你拿到提货单之后，没有必要一直在店里等着，可以先去干点其他事，比如看场电影；等看完电影后，
 * 基本上蛋糕也做好了，然后你就可以凭提货单领蛋糕了。
 *
 * 利用多线程可以快速将一些串行的任务并行化，从而提高性能；如果任务之间有依赖关系，
 * 比如当前任务依赖前一个任务的执行结果，这种问题基本上都可以用 Future 来解决。
 */
public class FutureTaskTest {
    @Test
    public void test3() throws ExecutionException, InterruptedException {
        // 创建任务 T2 的 FutureTask
        FutureTask<String> ft2
                = new FutureTask<>(new T2Task());
        // 创建任务 T1 的 FutureTask
        FutureTask<String> ft1
                = new FutureTask<>(new T1Task(ft2));
        // 线程 T1 执行任务 ft1
        Thread T1 = new Thread(ft1);
        T1.start();
        // 线程 T2 执行任务 ft2
        Thread T2 = new Thread(ft2);
        T2.start();
        // 等待线程 T1 执行结果
        System.out.println(ft1.get());
    }

    // T1Task 需要执行的任务：
    // 洗水壶、烧开水、泡茶
    class T1Task implements Callable<String> {
        FutureTask<String> ft2;

        // T1 任务需要 T2 任务的 FutureTask
        T1Task(FutureTask<String> ft2) {
            this.ft2 = ft2;
        }

        @Override
        public String call() throws Exception {
            System.out.println("T1: 洗水壶...");
            TimeUnit.SECONDS.sleep(1);

            System.out.println("T1: 烧开水...");
            TimeUnit.SECONDS.sleep(15);
            // 获取 T2 线程的茶叶
            String tf = ft2.get();
            System.out.println("T1: 拿到茶叶:" + tf);

            System.out.println("T1: 泡茶...");
            return " 上茶:" + tf;
        }
    }

    // T2Task 需要执行的任务:
    // 洗茶壶、洗茶杯、拿茶叶
    class T2Task implements Callable<String> {
        @Override
        public String call() throws Exception {
            System.out.println("T2: 洗茶壶...");
            TimeUnit.SECONDS.sleep(1);

            System.out.println("T2: 洗茶杯...");
            TimeUnit.SECONDS.sleep(2);

            System.out.println("T2: 拿茶叶...");
            TimeUnit.SECONDS.sleep(1);
            return " 龙井 ";
        }
    }
}

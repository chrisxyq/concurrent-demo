package threadPool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * 1、线程池中线程中异常尽量手动捕获
 * 2、通过设置ThreadFactory的UncaughtExceptionHandler可以对未捕获的异常做保底处理，
 * 通过execute提交任务，线程依然会中断，而通过submit提交任务，可以获取线程执行结果，线程异常会在get执行结果时抛出。
 */
public class ThreadPoolExceptionTest {
    /**
     * 如果要捕获那些没被业务代码捕获的异常，可以设置Thread类的uncaughtExceptionHandler属性。
     * 这时使用ThreadFactoryBuilder会比较方便，ThreadFactoryBuilder是guava提供的ThreadFactory生成器
     */
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(200), new ThreadFactoryBuilder().setNameFormat("customThread %d").build());
    private static ThreadPoolExecutor threadPoolExecutorHandleUncaught = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(200),
            new ThreadFactoryBuilder()
                    .setNameFormat("customThread %d")
                    .setUncaughtExceptionHandler((t, e) -> System.out.println("UncaughtExceptionHandler捕获到：" + t.getName() + "发生异常" + e.getMessage()))
                    .build());

    /**
     * 可见每次执行的线程都不一样，之前的线程都没有复用。原因是因为出现了未捕获的异常
     */
    @Test
    public void test() {
        IntStream.rangeClosed(1, 5).forEach(i -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            threadPoolExecutor.execute(() -> {
                int j = 1 / 0;
            });
        });
    }

    /**
     * 当异常捕获了，线程就可以复用了
     */
    @Test
    public void test1() {
        IntStream.rangeClosed(1, 5).forEach(i -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            threadPoolExecutor.execute(() -> {
                try {
                    int j = 1 / 0;
                } catch (Exception e) {
                    System.out.println(Thread.currentThread().getName() + " " + e.getMessage());
                }
            });
        });
    }
    /**
     * 捕获到异常，但是线程依旧没有复用
     * 通过submit提交线程，无法达到线程复用。
     */
    @Test
    public void test2() {
        IntStream.rangeClosed(1, 5).forEach(i -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            threadPoolExecutorHandleUncaught.execute(() -> {
                System.out.println("线程" + Thread.currentThread().getName() + "执行");
                int j = 1 / 0;
            });
        });
    }
    /**
     * 捕获到异常，线程成功复用
     * 通过submit提交线程可以屏蔽线程中产生的异常，达到线程复用。当get()执行结果时异常才会抛出。
     * 原因是通过submit提交的线程，当发生异常时，会将异常保存，待future.get();时才会抛出。
     */
    @Test
    public void test3() {
        IntStream.rangeClosed(1, 5).forEach(i -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Future<?> future = threadPoolExecutor.submit(() -> {
                System.out.println("线程" + Thread.currentThread().getName() + "执行");
                int j = 1 / 0;
            });
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }
}

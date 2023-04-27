package com.chrisxyq.concurrent.future;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import com.chrisxyq.concurrent.utils.JsonUtils;

import java.util.concurrent.*;

/**
 * 对于简单的并行任务，你可以通过“线程池 +Future”的方案来解决
 * 1.ThreadPoolExecutor 的 void execute(Runnable command) 方法，
 * 利用这个方法虽然可以提交任务，但是却没有办法获取任务的执行结果（execute() 方法没有返回值）
 * 2.通过 ThreadPoolExecutor 提供的 3 个 submit() 方法和 1 个 FutureTask 工具类来支持获得任务执行结果的需求
 */
@Slf4j
public class ThreadPoolSubmitTest {
    ExecutorService executor = Executors.newFixedThreadPool(1);

    @Data
    class Result {
        private int    code;
        private String msg;
    }

    class Task implements Runnable {
        Result r;

        /**
         * 通过构造函数传入 result
         *
         * @param r
         */
        Task(Result r) {
            this.r = r;
        }

        public void run() {
            r.setMsg("success");
        }
    }

    /**
     * Future使用
     * submit(Runnable task, T result)
     * result 相当于主线程和子线程之间的桥梁，通过它主子线程可以共享数据
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void test() throws ExecutionException, InterruptedException {
        // 创建 Result 对象 r
        Result r = new Result();
        r.setCode(0);
        // 提交任务
        Future<Result> future = executor.submit(new Task(r), r);
        Result futureRes = future.get();
        log.info("futureRes:{}", JsonUtils.toJson(futureRes));
    }

    /**
     * FutureTask 实现了 Runnable 和 Future 接口，由于实现了 Runnable 接口，
     * 所以可以将 FutureTask 对象作为任务提交给 ThreadPoolExecutor 去执行，
     * 也可以直接被 Thread 执行；又因为实现了 Future 接口，所以也能用来获得任务的执行结果。
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void test1() throws ExecutionException, InterruptedException {
        // 创建 FutureTask
        FutureTask<Integer> futureTask
                = new FutureTask<>(() -> 1 + 2);
        // 提交 FutureTask
        executor.submit(futureTask);
        // 获取计算结果
        log.info("futureTask.get():{}", futureTask.get());
    }

    @Test
    public void test2() throws ExecutionException, InterruptedException {
        Future<Integer> future = executor.submit(() -> 1 + 2);
        // 获取计算结果
        log.info("com.chrisxyq.concurrent.future.get():{}", future.get());
    }


}

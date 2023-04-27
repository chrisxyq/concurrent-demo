package com.chrisxyq.concurrent.future.completionservice;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 批量的并行任务，则可以通过 CompletionService 来解决
 * 当需要批量提交异步任务的时候建议你使用 CompletionService。
 * CompletionService 将线程池 Executor 和阻塞队列 BlockingQueue 的功能融合在了一起，
 * 能够让批量异步任务的管理更简单。除此之外，CompletionService 能够让异步任务的执行结果有序化，
 * 先执行完的先进入阻塞队列，利用这个特性，你可以轻松实现后续处理的有序性，避免无谓的等待
 * <p>
 * CompletionService 的实现原理也是内部维护了一个阻塞队列，
 * 当任务执行结束就把任务的执行结果加入到阻塞队列中，
 * 不同的是 CompletionService 是把任务执行结果的 Future 对象加入到阻塞队列中
 * <p>
 * CompletionService 的实现类 ExecutorCompletionService，需要你自己创建线程池，
 * 虽看上去有些啰嗦，但好处是你可以让多个 ExecutorCompletionService 的线程池隔离，
 * 这种隔离性能避免几个特别耗时的任务拖垮整个应用的风险。
 */
@Slf4j
public class CompletionServiceTest {
    ExecutorService executor =
            Executors.newFixedThreadPool(3);

    /**
     * 利用 CompletionService 来实现高性能的询价系统。
     * 其中，我们没有指定 completionQueue，因此默认使用无界的 LinkedBlockingQueue。
     * 之后通过 CompletionService 接口提供的 submit() 方法提交了三个询价操作，
     * 这三个询价操作将会被 CompletionService 异步执行。
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void test() throws ExecutionException, InterruptedException {
        // 创建 CompletionService
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        CompletionService<Integer> cs = new
                ExecutorCompletionService<>(executor);
        // 异步向电商 S1 询价
        cs.submit(() -> getPriceByS1());
        // 异步向电商 S2 询价
        cs.submit(() -> getPriceByS2());
        // 异步向电商 S3 询价
        cs.submit(() -> getPriceByS3());
        // 将询价结果异步保存到数据库
        for (int i = 0; i < 3; i++) {
            executor.execute(() -> {
                try {
                    Integer r = cs.take().get();
                    save(r);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }
        //主线程不需要等待异步保存数据库的结果
        log.info("消耗：{}ms", stopWatch.getTime());
        Thread.sleep(10000);
    }

    /**
     * 由于主线程需要获取最低报价
     * 使用CountDownLatch 等待
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void test1() throws ExecutionException, InterruptedException {
        // 创建 CompletionService
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        CompletionService<Integer> cs = new
                ExecutorCompletionService<>(executor);
        // 异步向电商 S1 询价
        cs.submit(() -> getPriceByS1());
        // 异步向电商 S2 询价
        cs.submit(() -> getPriceByS2());
        // 异步向电商 S3 询价
        cs.submit(() -> getPriceByS3());
        // 将询价结果异步保存到数据库
        // 并计算最低报价
        AtomicReference<Integer> minPrice = new AtomicReference<>(Integer.MAX_VALUE);
        CountDownLatch latch = new CountDownLatch(3);
        for (int i = 0; i < 3; i++) {
            executor.execute(() -> {
                try {
                    Integer r = cs.take().get();
                    latch.countDown();
                    minPrice.set(Integer.min(minPrice.get(), r));
                    save(r);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        }
        //主线程需要等待所有异步报价结果
        latch.await();
        log.info("消耗：{}ms，minPrice：{}", stopWatch.getTime(), minPrice);
    }

    /**
     * 每次通过调用 CompletionService 的 submit() 方法提交一个异步任务，
     * 会返回一个 Future 对象，我们把这些 Future 对象保存在列表 futures 中。
     * 通过调用 cs.take().get()，我们能够拿到最快返回的任务执行结果，
     * 只要我们拿到一个正确返回的结果，就可以取消所有任务并且返回最终结果了
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void test2() throws ExecutionException, InterruptedException {
        // 创建 CompletionService
        CompletionService<Integer> cs =
                new ExecutorCompletionService<>(executor);
        // 用于保存 Future 对象
        List<Future<Integer>> futures =
                new ArrayList<>(3);
        // 提交异步任务，并保存 com.chrisxyq.concurrent.future 到 futures
        futures.add(
                cs.submit(() -> getPriceByS1()));
        futures.add(
                cs.submit(() -> getPriceByS2()));
        futures.add(
                cs.submit(() -> getPriceByS3()));
        // 获取最快返回的任务执行结果
        Integer r = 0;
        try {
            // 只要有一个成功返回，则 break
            for (int i = 0; i < 3; ++i) {
                r = cs.take().get();
                // 简单地通过判空来检查是否成功返回
                if (r != null) {
                    break;
                }
            }
        } finally {
            // 取消所有任务
            for (Future<Integer> f : futures)
                f.cancel(true);
        }
        // 返回结果
        log.info("res:{}", r);
    }

    private void save(Integer r) throws InterruptedException {
        Thread.sleep(3000);
        log.info("save(),price:{}", r);
    }

    private Integer getPriceByS3() throws InterruptedException {
        Thread.sleep(3000);
        int price = 1;
        log.info("getPriceByS3(),current thread:{},price:{}", Thread.currentThread().getName(), price);
        return price;
    }

    private Integer getPriceByS2() throws InterruptedException {
        Thread.sleep(2000);
        int price = 2;
        log.info("getPriceByS2(),current thread:{},price:{}", Thread.currentThread().getName(), price);
        return price;
    }

    private Integer getPriceByS1() throws InterruptedException {
        Thread.sleep(1000);
        int price = 3;
        log.info("getPriceByS1(),current thread:{},price:{}", Thread.currentThread().getName(), price);
        return price;
    }
}

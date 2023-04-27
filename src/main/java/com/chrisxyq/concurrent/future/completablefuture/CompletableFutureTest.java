package com.chrisxyq.concurrent.future.completablefuture;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 如果任务之间有聚合关系，无论是 AND 聚合还是 OR 聚合，都可以通过 CompletableFuture 来解决
 */
@Slf4j
public class CompletableFutureTest {
    ExecutorService executor = Executors.newFixedThreadPool(10,
            new ThreadFactoryBuilder().setNameFormat("customThread %d").build());

    @Test
    public void test() throws ExecutionException, InterruptedException {
        List<CompletableFuture<HashMap<Integer, String>>> futureList = new ArrayList<>();
        IntStream.rangeClosed(0, 9).forEach(i -> {
            CompletableFuture<HashMap<Integer, String>> future = CompletableFuture.supplyAsync(() ->
                            getMap(i), executor)
                    .whenComplete((res, exception) ->
                            log.info("getMap({}) whenComplete", i))
                    .exceptionally(e -> {
                        log.warn("getMap({}) exceptionally", i, e);
                        return null;
                    });
            futureList.add(future);
        });
        //阻塞等待所有结果完成
        CompletableFuture<List<HashMap<Integer, String>>> listCompletableFuture = CompletableFuture
                .allOf(futureList.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        futureList.stream().
                                map(CompletableFuture::join).
                                collect(Collectors.<HashMap<Integer, String>>toList())
                );
        List<HashMap<Integer, String>> responseTypes = listCompletableFuture.get();
        log.info("responseTypes:{}", responseTypes);
    }

    private HashMap<Integer, String> getMap(int i) {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(i, Thread.currentThread().getName());
        return map;
    }

    /**
     * runAsync(Runnable runnable)和supplyAsync(Supplier<U> supplier)，
     * 它们之间的区别是：Runnable 接口的 run() 方法没有返回值，而 Supplier 接口的 get() 方法是有返回值的
     * <p>
     * 默认情况下 CompletableFuture 会使用公共的 ForkJoinPool 线程池，这个线程池默认创建的线程数是 CPU 的核数
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void test1() throws ExecutionException, InterruptedException {
        // 任务 1：洗水壶 -> 烧开水
        CompletableFuture<Void> f1 =
                CompletableFuture.runAsync(() -> {
                    log.info("current thread:{},洗水壶...", Thread.currentThread().getName());
                    sleep(1, TimeUnit.SECONDS);
                    log.info("current thread:{},烧开水...", Thread.currentThread().getName());
                    sleep(6, TimeUnit.SECONDS);
                });
        // 任务 2：洗茶壶 -> 洗茶杯 -> 拿茶叶
        CompletableFuture<String> f2 =
                CompletableFuture.supplyAsync(() -> {
                    log.info("current thread:{},洗茶壶...", Thread.currentThread().getName());
                    sleep(1, TimeUnit.SECONDS);
                    log.info("current thread:{},洗茶杯...", Thread.currentThread().getName());
                    sleep(2, TimeUnit.SECONDS);
                    log.info("current thread:{},拿茶叶...", Thread.currentThread().getName());
                    sleep(1, TimeUnit.SECONDS);
                    return " 龙井 ";
                });
        // 任务 3：任务 1 和任务 2 完成后执行：泡茶
        CompletableFuture<String> f3 =
                f1.thenCombine(f2, (__, tf) -> {
                    log.info("current thread:{},拿到茶叶:{}", Thread.currentThread().getName(), tf);
                    log.info("current thread:{},泡茶...", Thread.currentThread().getName());
                    return " 上茶:" + tf;
                });
        // 等待任务 3 执行结果
        log.info("current thread:{},res:{}", Thread.currentThread().getName(), f3.join());
    }

    void sleep(int t, TimeUnit u) {
        try {
            u.sleep(t);
        } catch (InterruptedException e) {
        }
    }

    @Test
    public void test2() throws ExecutionException, InterruptedException {
        CompletableFuture<String> f0 =
                CompletableFuture.supplyAsync(
                                () -> {
                                    log.info("current thread:{}", Thread.currentThread().getName());
                                    return "Hello World";
                                })      //①
                        .thenApply(s -> {
                            log.info("current thread:{}", Thread.currentThread().getName());
                            return s + " QQ";
                        })  //②
                        .thenApply(s -> {
                            log.info("current thread:{}", Thread.currentThread().getName());
                            return s.toUpperCase();
                        });//③
        log.info("current thread:{},res:{}", Thread.currentThread().getName(), f0.join());
    }

    /**
     * CompletableFuture.supplyAsync(
     * 默认使用ForkJoinPool.commonPool线程池
     * 异步调用
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void test11() throws ExecutionException, InterruptedException {
         Object o = new Object();
        //默认使用ForkJoinPool.commonPool线程池
        CompletableFuture<String> f0 =
                CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            log.info("current thread:{},o:{}", Thread.currentThread().getName(),o);
                            return "Hello World";
                        });
        log.info("current thread:{},res:{}", Thread.currentThread().getName(), f0.join());
    }

    @Test
    public void test3() throws ExecutionException, InterruptedException {
        System.out.println(Runtime.getRuntime().availableProcessors());
        CompletableFuture<String> f0 =
                CompletableFuture.supplyAsync(
                                () -> {
                                    log.info("current thread:{}", Thread.currentThread().getName());
                                    return "Hello World";
                                })      //①
                        .thenApplyAsync(s -> {
                            log.info("current thread:{}", Thread.currentThread().getName());
                            return s + " QQ";
                        })  //②
                        .thenApplyAsync(s -> {
                            log.info("current thread:{}", Thread.currentThread().getName());
                            return s.toUpperCase();
                        });//③
        log.info("current thread:{},res:{}", Thread.currentThread().getName(), f0.join());
    }

    /**
     * CompletableFuture异常处理
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void test4() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> f0 = CompletableFuture.supplyAsync(() -> 7 / 0)
                .thenApply(r -> r * 10)
                .exceptionally(e -> 0);
        System.out.println(f0.join());
    }

    /**
     * 使用 applyToEither() 方法来描述一个 OR 汇聚关系
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void test5() throws ExecutionException, InterruptedException {
        CompletableFuture<String> f1 =
                CompletableFuture.supplyAsync(() -> {
                    //org.apache.commons.lang3.RandomUtils 提供了如下产生指定范围的随机数方法:
                    int t = RandomUtils.nextInt(100, 200);
                    log.info("current thread:{},t:{}", Thread.currentThread().getName(), t);
                    sleep(t, TimeUnit.SECONDS);
                    return String.valueOf(t);
                });

        CompletableFuture<String> f2 =
                CompletableFuture.supplyAsync(() -> {
                    int t = RandomUtils.nextInt(1, 5);
                    log.info("current thread:{},t:{}", Thread.currentThread().getName(), t);
                    sleep(t, TimeUnit.SECONDS);
                    return String.valueOf(t);
                });

        CompletableFuture<String> f3 =
                f1.applyToEither(f2, s -> s);
        log.info("current thread:{},res:{}", Thread.currentThread().getName(), f3.join());
    }

    /**
     * 1，读数据库属于io操作，应该放在单独线程池，避免线程饥饿 共享线程池：有福同享就要有难同当
     * 2，异常未处理
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void test6() throws ExecutionException, InterruptedException {
        // 采购订单
        PurchersOrder po = null;
        CompletableFuture<Boolean> cf =
                CompletableFuture.supplyAsync(() -> {
                    // 在数据库中查询规则
                    return findRuleByJdbc();
                }).thenApply(r -> {
                    // 规则校验
                    return check(po, r);
                });
        Boolean isOk = cf.join();
    }

    private Boolean check(PurchersOrder po, Object r) {
        return r.equals("1");
    }

    class PurchersOrder {

    }

    private <U> U findRuleByJdbc() {
        int temp = 1 / 0;
        return null;
    }

    @Test
    public void test7() throws ExecutionException, InterruptedException {
        // 采购订单
        PurchersOrder po = null;
        CompletableFuture<Boolean> cf =
                CompletableFuture.supplyAsync(() -> {
                    // 在数据库中查询规则
                    return findRuleByJdbc();
                }).exceptionally(e -> {
                    log.warn("findRuleByJdbc() exceptionally", e);
                    return "1";
                }).thenApply(r -> {
                    // 规则校验
                    return check(po, r);
                });
        log.info("current thread:{},res:{}", Thread.currentThread().getName(), cf.join());
    }
}

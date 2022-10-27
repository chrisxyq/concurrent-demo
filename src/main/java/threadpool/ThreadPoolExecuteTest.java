package threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * 下面我们一一介绍这些参数的意义，你可以把线程池类比为一个项目组，而线程就是项目组的成员。
 * <p>
 * corePoolSize：表示线程池保有的最小线程数。有些项目很闲，
 * 但是也不能把人都撤了，至少要留 corePoolSize 个人坚守阵地。
 * <p>
 * maximumPoolSize：表示线程池创建的最大线程数。当项目很忙时，
 * 就需要加人，但是也不能无限制地加，最多就加到 maximumPoolSize 个人。
 * 当项目闲下来时，就要撤人了，最多能撤到 corePoolSize 个人。
 * <p>
 * keepAliveTime & unit：上面提到项目根据忙闲来增减人员，
 * 那在编程世界里，如何定义忙和闲呢？很简单，一个线程如果在一段时间内，
 * 都没有执行任务，说明很闲，keepAliveTime 和 unit 就是用来定义这个“一段时间”的参数。
 * 也就是说，如果一个线程空闲了keepAliveTime & unit这么久，而且线程池的线程数大于 corePoolSize ，那么这个空闲的线程就要被回收了。
 * <p>
 * workQueue：工作队列，和上面示例代码的工作队列同义。
 * threadFactory：通过这个参数你可以自定义如何创建线程，例如你可以给线程指定一个有意义的名字。
 * handler：通过这个参数你可以自定义任务的拒绝策略。如果线程池中所有的线程都在忙碌，
 * 并且工作队列也满了（前提是工作队列是有界队列），那么此时提交任务，线程池就会拒绝接收。
 * 至于拒绝的策略，你可以通过 handler 这个参数来指定。ThreadPoolExecutor 已经提供了以下 4 种策略。
 * CallerRunsPolicy：提交任务的线程自己去执行该任务。
 * AbortPolicy：默认的拒绝策略，会 throws RejectedExecutionException。
 * DiscardPolicy：直接丢弃任务，没有任何异常抛出。
 * DiscardOldestPolicy：丢弃最老的任务，其实就是把最早进入工作队列的任务丢弃，
 * 然后把新任务加入到工作队列。
 * Java 在 1.6 版本还增加了 allowCoreThreadTimeOut(boolean value) 方法，
 * 它可以让所有线程都支持超时，这意味着如果项目很闲，就会将项目组的成员都撤走。
 */
@Slf4j
public class ThreadPoolExecuteTest {
    private static final ExecutorService    threadPool                   = new ThreadPoolExecutor(
            2,
            Runtime.getRuntime().availableProcessors() + 1,
            2L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(3),
            Executors.defaultThreadFactory(),
            //拒绝策略implements RejectedExecutionHandler
            new ThreadPoolExecutor.AbortPolicy());

    private static final ExecutorService    threadPoolCustomRejectPolicy = new ThreadPoolExecutor(
            2,
            Runtime.getRuntime().availableProcessors() + 1,
            2L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(3),
            r->{
                //建议根据业务需求实现 ThreadFactory
                return new Thread(r, "echo-"+ r.hashCode());
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                    System.out.println("自定义拒绝策略");
                    throw new RejectedExecutionException("Task " + r.toString() +
                            " rejected from " +
                            e.toString());
                }
            });
    /**
     * ThreadFactoryBuilder是guava提供的ThreadFactory生成器
     */
    private static       ThreadPoolExecutor singleThreadPool             = new ThreadPoolExecutor(1, 1,
            60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(200),
            new ThreadFactoryBuilder().setNameFormat("customThread %d").build());

    /**
     * 使用线程池要注意:
     * 1.线程池默认的拒绝策略会 throw RejectedExecutionException 这是个运行时异常，
     * 对于运行时异常编译器并不强制 catch 它
     * <p>
     * 自定义拒绝策略
     * 可以写一个匿名类
     * 实现 implements RejectedExecutionHandler接口
     */
    @Test
    public void test() {

        try {
            //模拟有10个顾客过来办理业务，目前池子里有5个工作人员提供服务
            IntStream.rangeClosed(1, 10).forEach(i -> {
                threadPoolCustomRejectPolicy.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + "\t办理业务");
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPoolCustomRejectPolicy.shutdown();
        }
        //RejectedExecutionException
        threadPoolCustomRejectPolicy.execute(() -> {
            System.out.println(Thread.currentThread().getName() + "\t办理业务");
        });
    }

    /**
     * 使用线程池要注意:
     * 2.考虑到 ThreadPoolExecutor 的构造函数实在是有些复杂，
     * 所以 Java 并发包里提供了一个线程池的静态工厂类 Executors，
     * 利用 Executors 你可以快速创建线程池。不过目前大厂的编码规范中基本上都不建议使用 Executors 了，
     * <p>
     * 不建议使用 Executors 的最重要的原因是：
     * Executors 提供的很多方法默认使用的都是无界的 LinkedBlockingQueue，
     * 高负载情境下，无界队列很容易导致 OOM，而 OOM 会导致所有请求都无法处理
     */
    @Test
    public void test1() {
        //一个池子有5个工作线程，类似银行有5个受理窗口
        //ExecutorService threadPool = Executors.newFixedThreadPool(5);
        //一个池子有1个工作线程，类似银行有1个受理窗口
        //ExecutorService threadPool = Executors.newSingleThreadExecutor();
        //一个池子有n个工作线程，类似银行有n个受理窗口
        ExecutorService threadPool = Executors.newCachedThreadPool();
        try {
            //模拟有10个顾客过来办理业务，目前池子里有5个工作人员提供服务
            IntStream.rangeClosed(1, 10).forEach(i -> {
                threadPool.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + "\t办理业务");
                });
                //暂停毫秒
                //try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) { e.printStackTrace(); }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    /**
     * 使用线程池要注意:
     * 3.使用线程池，还要注意异常处理的问题，例如通过 ThreadPoolExecutor 对象的 execute() 方法提交任务时，
     * 如果任务在执行的过程中出现运行时异常，会导致执行任务的线程终止；
     * 不过，最致命的是任务虽然异常了，但是你却获取不到任何通知，这会让你误以为任务都执行得很正常。
     * 虽然线程池提供了很多用于异常处理的方法，但是最稳妥和简单的方案还是捕获所有异常并按需处理
     */
    @Test
    public void test2() {
        IntStream.rangeClosed(1, 10).forEach(i -> {
            singleThreadPool.execute(() -> {
                // 业务逻辑
                System.out.println(Thread.currentThread().getName() + "\t办理业务");
                int temp = 1 / 0;
            });
        });

    }

    /**
     * 捕获到异常，线程成功复用
     */
    @Test
    public void test3() {
        IntStream.rangeClosed(1, 10).forEach(i -> {
            singleThreadPool.execute(() -> {
                try {
                    // 业务逻辑
                    System.out.println(Thread.currentThread().getName() + "\t办理业务");
                } catch (RuntimeException x) {
                    // 按需处理
                } catch (Throwable x) {
                    // 按需处理
                }
            });
        });
    }
}

package avoidshare.threadlocal;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copy-on-Write 模式和线程本地存储模式本质上都是为了避免共享
 * ThreadLocal 解决方案的具体实现，这段代码与前面 ThreadId 的代码高度相似，
 * 同样地，不同线程调用 SafeDateFormat 的 get() 方法将返回不同的 SimpleDateFormat 对象实例，
 * 由于不同线程并不共享 SimpleDateFormat，所以就像局部变量一样，是线程安全的。
 */
class SafeDateFormat {
    // 定义 ThreadLocal 变量
    static final ThreadLocal<DateFormat> tl = ThreadLocal.withInitial(() ->
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    static DateFormat get() {
        return tl.get();
    }
}

/**
 * 为每个线程分配一个唯一的 Id
 */
class ThreadId {
    static final AtomicLong        nextId = new AtomicLong(0);
    // 定义 ThreadLocal 变量
    static final ThreadLocal<Long> tl     = ThreadLocal.withInitial(() -> nextId.getAndIncrement());

    // 此方法会为每个线程分配一个唯一的 Id
    static long get() {
        return tl.get();
    }
}

@Slf4j
public class ThreadLocalTest {
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ExecutorService threadPool = Executors.newFixedThreadPool(3);

    /**
     * SimpleDateFormat是线程不安全的
     * SimpleDateFormat是Java提供的一个格式化和解析日期的工具类，日常开发中应该经常会用到，
     * 但是由于它是线程不安全的，多线程公用一个SimpleDateFormat实例对日期进行解析或者格式化会导致程序出错
     */
    @Test
    public void test() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; ++i) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        log.info("res:{}", sdf.parse("2017-12-13 15:17:27"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
            thread.start();// (4)启动线程
        }
        latch.await();
    }

    /**
     * SimpleDateFormat是线程不安全的
     *  不同线程执行下面代码,返回的 SimpleDateFormat 是不同的
     * @throws InterruptedException
     */
    @Test
    public void test1() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; ++i) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        // 不同线程执行下面代码,返回的 df 是不同的
                        DateFormat sdf = SafeDateFormat.get();
                        log.info("res:{}", sdf.parse("2017-12-13 15:17:27"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
            thread.start();// (4)启动线程
        }
        latch.await();
    }

    /**
     * 下面这个静态类 ThreadId 会为每个线程分配一个唯一的线程 Id，
     * 如果一个线程前后两次调用 ThreadId 的 get() 方法，
     * 两次 get() 方法的返回值是相同的。但如果是两个线程分别调用 ThreadId 的 get() 方法，
     * 那么两个线程看到的 get() 方法的返回值是不同的。
     * @throws InterruptedException
     */
    @Test
    public void test2() throws InterruptedException {
        int taskNum = 10;
        CountDownLatch latch = new CountDownLatch(taskNum);
        for (int i = 0; i < taskNum; ++i) {
            threadPool.submit(() -> {
                log.info("current thread:{},ThreadId.get():{}",
                        Thread.currentThread().getName(),
                        ThreadId.get());
                latch.countDown();
            });
        }
        latch.await();
    }
}

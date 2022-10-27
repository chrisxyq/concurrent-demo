package threadlocal;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thread 这个类内部有一个私有属性: threadLocals
 * ThreadLocal.ThreadLocalMap threadLocals
 * 其类型就是 ThreadLocalMap，ThreadLocalMap 的 Key 是 ThreadLocal。
 *
 * Spring 使用 ThreadLocal 来传递事务信息。
 * 那你觉得在异步场景中，是否可以使用 Spring 的事务管理器呢？
 * 不可以，因为ThreadLocal内的变量是线程级别的，而异步编程意味着线程不同，不同线程的变量不可以共享
 */
@Slf4j
public class ThreadLocalMemoryLeakTest {
    ExecutorService threadPool = Executors.newFixedThreadPool(3);

    /**
     * 在线程池中使用 ThreadLocal，如果不谨慎就可能导致内存泄露。
     * 线程池中线程的存活时间太长，往往都是和程序同生共死的，
     * 这就意味着 Thread 持有的 ThreadLocalMap 一直都不会被回收，
     * 再加上 ThreadLocalMap 中的 Entry 对 ThreadLocal 是弱引用（WeakReference），
     * 所以只要 ThreadLocal 结束了自己的生命周期是可以被回收掉的。
     * 但是 Entry 中的 Value 却是被 Entry 强引用的，
     * 所以即便 Value 的生命周期结束了，Value 也是无法被回收的，从而导致内存泄露。
     */
    @Test
    public void test1() throws InterruptedException {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadPool.execute(() -> {
            //ThreadLocal 增加变量
            threadLocal.set("value");
            try {
                // 省略业务逻辑代码
            } finally {
                // 手动清理 ThreadLocal
                //从当前线程的ThreadLocalMap中移除当前的entry
                threadLocal.remove();
            }
        });
    }
}


package semaphore;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import utils.JsonUtils;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

/**
 * 比较常见的需求就是我们工作中遇到的各种池化资源，
 * 例如连接池、对象池、线程池等等。
 * 其中，你可能最熟悉数据库连接池，在同一时刻，
 * 一定是允许多个线程同时使用连接池的，当然，每个连接在被释放前，是不允许其他线程使用的。
 * @param <T>
 * @param <R>
 */
@Slf4j
class ObjPool<T, R> {
    /**
     * 我们用一个 List来保存对象实例，用 Semaphore 实现限流器。
     * 关键的代码是 ObjPool 里面的 exec() 方法，
     * 这个方法里面实现了限流的功能。在这个方法里面，
     * 我们首先调用 acquire() 方法（与之匹配的是在 finally 里面调用 release() 方法），
     * 假设对象池的大小是 10，信号量的计数器初始化为 10，
     * 那么前 10 个线程调用 acquire() 方法，都能继续执行，
     * 相当于通过了信号灯，而其他线程则会阻塞在 acquire() 方法上。
     * 对于通过信号灯的线程，我们为每个线程分配了一个对象 t（这个分配工作是通过 pool.remove(0) 实现的），
     * 分配完之后会执行一个回调函数 func，而函数的参数正是前面分配的对象 t ；
     * 执行完回调函数之后，它们就会释放对象（这个释放工作是通过 pool.add(t) 实现的），
     * 同时调用 release() 方法来更新信号量的计数器。如果此时信号量里计数器的值小于等于 0，
     * 那么说明有线程在等待，此时会自动唤醒等待的线程。
     */
    final List<T>   pool;
    // 用信号量实现限流器
    final Semaphore sem;

    /**
     * 构造函数
     * Semaphore 可以允许多个线程访问一个临界区，
     * 那就意味着可能存在多个线程同时访问 ArrayList，
     * 而 ArrayList 不是线程安全的，
     * 所以对象池的例子中是不能够将 Vector 换成 ArrayList 的。
     * Semaphore 允许多个线程访问一个临界区，这也是一把双刃剑，
     * 当多个线程进入临界区时，如果需要访问共享变量就会存在并发问题，
     * 所以必须加锁，也就是说 Semaphore 需要锁中锁。
     * @param size
     * @param t
     */
    ObjPool(int size, T t) {
        pool = new Vector<T>() {
        };
        for (int i = 0; i < size; i++) {
            pool.add(t);
        }
        sem = new Semaphore(size);
    }

    /**
     * 利用对象池的对象，调用 func
     * 入参类型为T
     * 返回值类型为R
     *
     * @param func
     * @return
     * @throws InterruptedException
     */
    R exec(Function<T, R> func) throws InterruptedException {
        T t = null;
        sem.acquire();
        //要注意：由于在sem.acquire()和   sem.release()中间的代码会有多个线程同时进入，
        // 因此打印日志顺序和debug会出现混乱
        try {
            t = pool.remove(0);
            log.info("ObjPool acquired by thread:{},pool:{}",
                    Thread.currentThread().getName(), pool);
            return func.apply(t);
        } finally {
            pool.add(t);
            log.info("ObjPool give back by thread:{},pool:{}",
                    Thread.currentThread().getName(), pool);
            sem.release();
        }
    }
}

@Slf4j
public class ObjPoolBySemaphore {
    @Test
    public void test() throws InterruptedException {
        // 创建对象池
        ObjPool<Long, String> pool = new ObjPool<Long, String>(3, 1L);
        // 通过对象池获取 t，之后执行
        for (int i = 1; i <= 5; i++) {
            new Thread(() -> {
                try {
                    pool.exec(t -> {
                        System.out.println(t);
                        return t.toString();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, String.valueOf(i)).start();
            //Thread.sleep(1000);
        }
        //等待所有线程结束
        Thread.sleep(7000);
    }
}

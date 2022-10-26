package nolock.atomic;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 无锁方案则完全没有加锁、解锁的性能消耗，同时还能保证互斥性
 * CPU 为了解决并发问题，提供了 CAS 指令（CAS，全称是 Compare And Swap，即“比较并交换”）。
 * CAS 指令包含 3 个参数：共享变量的内存地址 A、用于比较的值 B 和共享变量的新值 C；
 * 并且只有当内存中地址 A 处的值等于 B 时，才能将内存中地址 A 处的值更新为新值 C。
 * 作为一条 CPU 指令，CAS 指令本身是能够保证原子性的。
 * <p>
 * 使用硬件同步原语来代替锁
 * 这种方法它只适合于线程之间碰撞不太频繁，也就是说绝大部分情况下，执行 CAS 原语不需要重试这样的场景
 */
@Slf4j
public class AtomicTest {
    AtomicLong count = new AtomicLong(0);

    void add10K() {
        int idx = 0;
        while (idx++ < 10000) {
            count.getAndIncrement();
        }
    }

    @Test
    public void testBase() {
        AtomicInteger atomicInteger = new AtomicInteger(5);

        atomicInteger.getAndIncrement(); // 原子化 i++
        atomicInteger.getAndDecrement(); // 原子化的 i--
        atomicInteger.incrementAndGet(); // 原子化的 ++i
        atomicInteger.decrementAndGet(); // 原子化的 --i
        // 当前值 +=delta，返回 += 前的值
        int delta=2;
        atomicInteger.getAndAdd(delta);
        // 当前值 +=delta，返回 += 后的值
        atomicInteger.addAndGet(delta);
        //CAS 操作，返回是否成功
        atomicInteger.compareAndSet(1, 2);
        // 以下四个方法
        // 新值可以通过传入 func 函数来计算
        //atomicInteger.getAndUpdate(func);
        //atomicInteger.updateAndGet(func);
        //atomicInteger.getAndAccumulate(x, func);
        //atomicInteger.accumulateAndGet(x, func);
    }

    /**
     * 5. 原子化的累加器
     * DoubleAccumulator、DoubleAdder、LongAccumulator 和 LongAdder，
     * 这四个类仅仅用来执行累加操作，相比原子化的基本数据类型，速度更快，
     * 但是不支持 compareAndSet() 方法。如果你仅仅需要累加操作，使用原子化的累加器性能会更好。
     */
    @Test
    public void test1() {

    }
}

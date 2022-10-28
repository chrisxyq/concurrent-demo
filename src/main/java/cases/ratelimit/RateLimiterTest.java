package cases.ratelimit;

import com.atlassian.guava.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Guava 采用的是令牌桶算法，其核心是要想通过限流器，必须拿到令牌。也就是说，只要我们能够限制发放令牌的速率，那么就能控制流速了
 */
@Slf4j
public class RateLimiterTest {
    // 执行任务的线程池
    ExecutorService threadPool = Executors.newFixedThreadPool(1);
    @Test
    public void test(){
        // 限流器流速：2 个请求 / 秒
        RateLimiter limiter = RateLimiter.create(2.0);

        // 记录上一次执行时间
        final AtomicLong prev = new AtomicLong(System.nanoTime());
        // 测试执行 20 次
        for (int i=0; i<20; i++){
            // 限流器限流
            limiter.acquire();
            // 提交任务异步执行
            threadPool.execute(()->{
                long cur=System.nanoTime();
                // 打印时间间隔：毫秒
                System.out.println((cur-prev.get())/1000000);
                prev.set(cur);
            });
        }
    }
}

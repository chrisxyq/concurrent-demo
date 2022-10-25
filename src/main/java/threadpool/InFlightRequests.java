package threadpool;

import org.junit.Test;
import threadpool.dto.ResponseFuture;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.*;

public class InFlightRequests implements Closeable {
    private final static long                         TIMEOUT_SEC              = 10L;
    /**
     * 在实现异步网络传输的时候，一定要配套实现一个背压的机制，避免客户端请求速度过快，导致大量的请求失败。
     * 在服务端处理不过来的时候限制一下客户端的请求速度。
     * 这个信号量有 10 个许可，我们每次往 inFlightRequest 中加入一个 ResponseFuture 的时候，
     * 需要先从信号量中获得一个许可，如果这时候没有许可了，就会阻塞当前这个线程，也就是发送请求的这个线程，
     * 直到有人归还了许可，才能继续发送请求。我们每结束一个在途请求，就归还一个许可，
     * 这样就可以保证在途请求的数量最多不超过 10 个请求，积压在服务端正在处理或者待处理的请求也不会超过 10 个。
     */
    private final        Semaphore                    semaphore                = new Semaphore(10);
    /**
     * 用于维护所有的在途请求，key为请求id，value为返回值
     */
    private final        Map<Integer, ResponseFuture> futureMap                = new ConcurrentHashMap<>();
    private final        ScheduledExecutorService     scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final        ScheduledFuture              scheduledFuture;

    public InFlightRequests() {
        /**
         * 初始化时延时0s开始执行，本次执行结束后延迟1s开始下次执行
         */
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this::removeTimeoutFutures, 0, 1, TimeUnit.SECONDS);
    }

    public void put(ResponseFuture responseFuture) throws InterruptedException, TimeoutException {
        if (semaphore.tryAcquire(TIMEOUT_SEC, TimeUnit.SECONDS)) {
            futureMap.put(responseFuture.getRequestId(), responseFuture);
        } else {
            throw new TimeoutException();
        }
    }

    /**
     * 即使是我们对所有能捕获的异常都做了处理，也不能保证所有 ResponseFuture 都能正常或者异常结束，
     * 比如说，编写对端程序的程序员写的代码有问题，收到了请求就是没给我们返回响应，
     * 为了应对这种情况，还必须有一个兜底超时的机制来保证所有情况下 ResponseFuture 都能结束，
     * 无论什么情况，只要超过了超时时间还没有收到响应，我们就认为这个 ResponseFuture 失败了，结束并删除它
     */
    private void removeTimeoutFutures() {
        futureMap.entrySet().removeIf(entry -> {
            if (System.nanoTime() - entry.getValue().getTimestamp() > (TIMEOUT_SEC - 5) * 1000000000L) {
                System.out.println("semaphore.release():" + entry.getValue().getRequestId());
                semaphore.release();
                return true;
            } else {
                return false;
            }
        });
    }

    public ResponseFuture remove(int requestId) {
        ResponseFuture future = futureMap.remove(requestId);
        if (null != future) {
            semaphore.release();
        }
        return future;
    }

    @Override
    public void close() {
        scheduledFuture.cancel(true);
        scheduledExecutorService.shutdown();
    }

    @Test
    public void test() throws InterruptedException, TimeoutException {
        InFlightRequests inFlightRequests = new InFlightRequests();
        for (int i = 0; i < 15; i++) {
            System.out.println(i);
            inFlightRequests.put(new ResponseFuture(i, new CompletableFuture<>()));
        }
        inFlightRequests.close();
    }
}

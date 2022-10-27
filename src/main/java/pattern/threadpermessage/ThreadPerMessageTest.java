package pattern.threadpermessage;

import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ThreadPerMessageTest {
    /**
     * Thread-Per-Message 模式的一个最经典的应用场景是网络编程里服务端的实现，
     * 服务端为每个客户端请求创建一个独立的线程，当线程处理完请求后，自动销毁，
     * 这是一种最简单的并发处理网络请求的方法。
     *
     * Go 语言、Lua 语言里的协程，本质上就是一种轻量级的线程。
     * 轻量级的线程，创建的成本很低，基本上和创建一个普通对象的成本相似；
     * 并且创建的速度和内存占用相比操作系统线程至少有一个数量级的提升，
     * 所以基于轻量级线程实现 Thread-Per-Message 模式就完全没有问题了。
     * @throws IOException
     */
    @Test
    public void test() throws IOException {
        final ServerSocketChannel ssc =
                ServerSocketChannel.open().bind(
                        new InetSocketAddress(8080));
        // 处理请求
        try {
            while (true) {
                // 接收请求
                SocketChannel sc = ssc.accept();
                // 每个请求都创建一个线程
                new Thread(()->{
                    try {
                        // 读 Socket
                        ByteBuffer rb = ByteBuffer
                                .allocateDirect(1024);
                        sc.read(rb);
                        // 模拟处理请求
                        Thread.sleep(2000);
                        // 写 Socket
                        ByteBuffer wb =
                                (ByteBuffer)rb.flip();
                        sc.write(wb);
                        // 关闭 Socket
                        sc.close();
                    }catch(Exception e){
                    }
                }).start();
            }
        } finally {
            ssc.close();
        }
    }
}

package blockingqueue;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import utils.JsonUtils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 阻塞队列
 * 1        两个数据结构：栈/队列
 *  1.1 栈       后进先出
 *  1.2 队列      先进先出
 *  1.3总结
 * 2        阻塞队列
 *  2.1 阻塞      必须要阻塞/不得不阻塞
 * 3    how
 */
@Slf4j
public class BlockingQueueDemo12 {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(3);
/*
        System.out.println(blockingQueue.add("a"));
        System.out.println(blockingQueue.add("b"));
        System.out.println(blockingQueue.add("c"));
//        System.out.println(blockingQueue.add("x"));

        System.out.println(blockingQueue.remove());
        System.out.println(blockingQueue.remove());
        System.out.println(blockingQueue.remove());
//        System.out.println(blockingQueue.remove());
*/
/*
        System.out.println(blockingQueue.add("a"));
        System.out.println(blockingQueue.add("b"));
        System.out.println(blockingQueue.element());//对首元素

        System.out.println(blockingQueue.offer("a"));
        System.out.println(blockingQueue.offer("b"));
        System.out.println(blockingQueue.offer("c"));
        System.out.println(blockingQueue.offer("x"));

        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());
        System.out.println(blockingQueue.poll());
*/
/*
        blockingQueue.put("a");
        blockingQueue.put("a");
        blockingQueue.put("a");
//        blockingQueue.put("a");//阻塞

        System.out.println(blockingQueue.take());
        System.out.println(blockingQueue.take());
        System.out.println(blockingQueue.take());
//        System.out.println(blockingQueue.take());//阻塞
*/
        System.out.println(blockingQueue.offer("a"));
        System.out.println(blockingQueue.offer("a"));
        System.out.println(blockingQueue.offer("a"));
        System.out.println(blockingQueue.offer("a", 3L, TimeUnit.SECONDS));//等3秒

    }
    @Test
    public void test() throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);
        queue.put(0);
        queue.put(1);
        queue.put(2);
        log.info("queue:{}", JsonUtils.toJson(queue));
         Thread thread = new Thread(() -> {
            try {
                queue.take();
                log.info("queue.take(),queue:{},queue.size():{}", JsonUtils.toJson(queue),queue.size());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        queue.put(3);
        thread.join();
        log.info("queue:{}", JsonUtils.toJson(queue));
    }
}

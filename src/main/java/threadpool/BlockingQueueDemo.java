package threadPool;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author chrisxu
 * @create 2021-10-07 13:05
 * Ctrl + Alt + L：格式化代码
 * ctrl + Alt + T：代码块包围
 * ctrl + Y：删除行
 * ctrl + D：复制行
 * alt+上/下：移动光标到上/下方法
 * ctrl+shift+/：注释多行
 */
public class BlockingQueueDemo {
    public static void main(String[] args) {
        ArrayBlockingQueue queue = new ArrayBlockingQueue<>(3);
        System.out.println(queue.add("a"));
        System.out.println(queue.add("b"));
        System.out.println(queue.add("c"));
        //a
        System.out.println(queue.element());
        //java.lang.IllegalStateException: Queue full
        System.out.println(queue.add("a"));
    }
}

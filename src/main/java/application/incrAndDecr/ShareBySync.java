package application.incrAndDecr;

/**
 * @author chrisxu
 * @create 2021-10-04 23:30
 * Ctrl + Alt + L：格式化代码
 * ctrl + Alt + T：代码块包围
 * ctrl + Y：删除行
 * ctrl + D：复制行
 * alt+上/下：移动光标到上/下方法
 * ctrl+shift+/：注释多行
 */
public class ShareBySync {
    private int number = 0;

    public synchronized void incr() throws InterruptedException {
        //等待
        while (number != 0) {
            this.wait();
        }
        //干活
        number++;
        System.out.println(Thread.currentThread().getName() + "::" + number);
        //通知其他线程
        this.notifyAll();
    }

    public synchronized void decr() throws InterruptedException {
        //等待
        while (number != 1) {
            this.wait();
        }
        //干活
        number--;
        System.out.println(Thread.currentThread().getName() + "::" + number);
        //通知其他线程
        this.notifyAll();
    }
}

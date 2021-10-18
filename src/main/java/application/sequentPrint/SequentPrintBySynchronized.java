package application.sequentPrint;

/**
 * 三个线程共同完成打印1~1000
 *
 * @author chrisxu
 * @create 2020-05-29 19:22
 * 格式化代码: Ctrl + Alt + L
 * ctrl+alt+T：代码块包围
 * 去掉空白: Ctrl + Shift + J
 * alt+enter报错时提示解决方案\引入局部变量补全
 */
class Number implements Runnable {
    private int i = 1;

    @Override
    public void run() {
        while (true) {
//            notifyAll();
            synchronized (this) {

                //this调用,且只能用于同步代码块中
                notifyAll();
                if (i <= 100) {
                    System.out.println(Thread.currentThread().getName() + ':' + i);
                    i++;
                } else {
                    break;
                }
                try {
                    //this调用
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

/**
 * @author yuanqixu
 */
public class SequentPrintBySynchronized {
    public static void main(String[] args) {
        Number num = new Number();
        //num不要忘记
        Thread t1 = new Thread(num);
        Thread t2 = new Thread(num);
        Thread t3 = new Thread(num);
        t1.start();
        t2.start();
        t3.start();
    }

}

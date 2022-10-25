package cases.producerAndConsumer;

/**
 * wait notify实现生产者消费者
 * 生产者消费者问题：线程通信应用经典例题
 *
 * @author chrisxu
 * @create 2020-05-29 21:09
 * 格式化代码: Ctrl + Alt + L
 * ctrl+alt+T：代码块包围
 * 去掉空白: Ctrl + Shift + J
 * alt+enter报错时提示解决方案\引入局部变量补全
 */
class Clerk {
    public static int cnt = 0;

    public synchronized void produce() {
        if (cnt < 20) {
            cnt += 1;
            System.out.println(Thread.currentThread().getName() + "当前数量" + cnt);
            notify();
        } else {
            //等待
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void consume() {
        notify();
        if (cnt > 0) {
            cnt -= 1;
            System.out.println(Thread.currentThread().getName() + "当前数量" + cnt);
            notify();
        } else {
            //等待
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Producer extends Thread {
    //需要声明clerk类型的变量
    private Clerk c;

    public Producer(Clerk c) {
        this.c = c;
    }

    @Override
    public void run() {
        super.run();
        System.out.println(currentThread().getName() + "开始生产产品");
        while (true) {
            try {
                sleep(10);

            } catch (Exception e) {
                e.printStackTrace();
            }
            c.produce();
        }
    }
}

class Consumer1 extends Thread {
    private Clerk c;

    public Consumer1(Clerk c) {
        this.c = c;

    }

    @Override
    public void run() {
        super.run();
        System.out.println(currentThread().getName() + "开始消费产品");
        while (true) {
            try {
                sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            c.consume();
        }
    }
}

/**
 * @author yuanqixu
 */
public class PacByWaitNotify {
    public static void main(String[] args) {
        Clerk clerk = new Clerk();
        Producer producer = new Producer(clerk);
        Consumer1 consumer1 = new Consumer1(clerk);
        Consumer1 consumer2 = new Consumer1(clerk);
        producer.setName("生产者");
        consumer1.setName("消费者1");
        consumer2.setName("消费者2");
        producer.start();
        consumer1.start();
        consumer2.start();


    }
}

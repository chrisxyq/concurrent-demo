package basic.others;

/**
 * 创建多线程的方式二：实现Runnable接口
 * 1.创建一个实现了Runnable接口的类
 * 2.实现类去实现Runnable的抽象方法
 * 3.创建实现类的对象
 * 4.将此对象作为参数传递到Thread类的构造器中，创建Thread类的对象
 * 5.通过Thread类的对象调用start()
 *
 * @author chrisxu
 * @create 2020-05-29 14:25
 * 格式化代码: Ctrl + Alt + L
 * 去掉空白: Ctrl + Shift + J
 * alt+enter报错时提示解决方案\引入局部变量补全
 */

class Mthread implements Runnable {

    @Override
    //2.实现类去实现Runnable的抽象方法
    public void run() {
        for (int i = 0; i < 100; i++) {
            if (i % 2 == 0) {
                System.out.println(Thread.currentThread().getName() + ':' + i);
            }
        }
    }
}

/**
 * @author yuanqixu
 */
public class ThreadTest2 {
    //3.创建实现类的对象
    public static void main(String[] args) {
        Mthread mthread = new Mthread();
        //4.将此对象作为参数传递到Thread类的构造器中，创建Thread类的对象
        Thread T1 = new Thread(mthread);
        Thread T2 = new Thread(mthread);
        Thread T3 = new Thread(mthread);
        //5.通过Thread类的对象调用start() -->调用了Runnable类型的target的run()
        T1.start();
        T2.start();
        T3.start();


    }
}

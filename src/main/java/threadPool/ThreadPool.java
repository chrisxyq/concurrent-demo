package threadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池创建多线程
 *
 * @author chrisxu
 * @create 2020-05-29 22:15
 * 格式化代码: Ctrl + Alt + L
 * ctrl+alt+T：代码块包围
 * 去掉空白: Ctrl + Shift + J
 * alt+enter报错时提示解决方案\引入局部变量补全
 */

class NumberThread implements Runnable {
    @Override
    public void run() {
        for (int i = 0; i <= 100; i++) {
            if (i % 2 == 0) {
                System.out.println(Thread.currentThread().getName() + ":" + i);
            }

        }
    }
}

public class ThreadPool {
    public static void main(String[] args) {
        //Executors是工具类，ExecutorService是接口
        //1.提供指定数量的线程池
        ExecutorService service = Executors.newFixedThreadPool(10);

        //设置线程池的属性
//        System.out.println(service.getClass());//ThreadPoolExecutor
        //先进行类型转换
//        ThreadPoolExecutor service1= (ThreadPoolExecutor) service;
//        service1.setCorePoolSize(12);


        //2.执行指定线程的操作，需要提供实现Runnable接口或者Callable接口的实现类的对象
        service.execute(new NumberThread());//适合Runnable
//        service.submit();//适合Callable
        service.shutdown();//3.关闭线程池


    }
}

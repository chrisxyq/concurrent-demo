package basic.others;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

class RunnableThread implements Runnable {
    @Override
    public void run() {

    }
}

class CallableThread implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println("******come in call method()");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 1080;
    }
}

/**
 * 多线程中，第3种获得多线程的方式
 * 1.get方法一般放在最后一行
 */
public class CallableTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<Integer> futureTask = new FutureTask(new CallableThread());
        new Thread(futureTask, "A").start();
        System.out.println(Thread.currentThread().getName() + "******计算完成");
        while (!futureTask.isDone()) {
            System.out.println("wait...");
        }
        System.out.println(futureTask.get());
        System.out.println(futureTask.get());
    }

    @Test
    public void test() {
        new Thread(new RunnableThread(), "aa").start();
    }

    @Test
    public void test2() {
        FutureTask<Integer> task = new FutureTask<>(new CallableThread());
        //lam表达式
        FutureTask<Integer> task2 = new FutureTask<>(() -> {
            return 1024;
        });
    }

}
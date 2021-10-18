package locks;

/**
 *演示线程的死锁问题
 *
 *
 * @author chrisxu
 * @create 2020-05-29 17:44
 * 格式化代码: Ctrl + Alt + L
 * 去掉空白: Ctrl + Shift + J
 * alt+enter报错时提示解决方案\引入局部变量补全
 */
public class DeadLockDemo {
    public static void main(String[] args) {
        final StringBuffer s1=new StringBuffer();
        final StringBuffer s2=new StringBuffer();

        //继承的方式
        new Thread(){
                @Override
                public void run() {
                    super.run();
                    synchronized (s1){
                        s1.append("a");
                        s2.append("1");
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        synchronized (s2){
                            s1.append("b");
                            s2.append("2");
                            System.out.println(s1);
                            System.out.println(s2);
                        }
                    }
                }
        }.start();

        //实现Runnable接口的类的对象
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (s2){
                    s1.append("c");
                    s2.append("3");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    synchronized (s1){
                        s1.append("d");
                        s2.append("4");
                        System.out.println(s1);
                        System.out.println(s2);
                    }
                }

            }
        }).start();




    }
}

package basic;

/**
 * 编译优化带来的有序性问题
 */
public class Singleton {
    /**
     * new 操作指令重排：
     *
     * 分配一块内存 M；
     * 将 M 的地址赋值给 instance 变量；
     * 最后在内存 M 上初始化 Singleton 对象。
     */
    static volatile Singleton instance;

    /**
     * 在双重检查方案中，一旦 Singleton 对象被成功创建之后，
     * 就不会执行 synchronized(Singleton.class){}相关的代码，
     * 也就是说，此时 getInstance() 方法的执行路径是无锁的，从而解决了性能问题。
     * @return
     */
    static Singleton getInstance(){
        if (instance == null) {
            synchronized(Singleton.class) {
                if (instance == null)
                    instance = new Singleton();
            }
        }
        /**
         * 若不用 volatile
         * 此时的 instance 是没有初始化过的，
         * 如果我们这个时候访问 instance 的成员变量就可能触发空指针异常。
         */
        return instance;
    }
}

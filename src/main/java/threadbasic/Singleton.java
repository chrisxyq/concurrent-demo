package threadbasic;

public class Singleton {
    /**
     * new 操作指令重排：
     *
     * 分配一块内存 M；
     * 将 M 的地址赋值给 instance 变量；
     * 最后在内存 M 上初始化 Singleton 对象。
     */
    static volatile Singleton instance;
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

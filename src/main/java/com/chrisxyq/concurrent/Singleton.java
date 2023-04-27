package com.chrisxyq.concurrent;

/**
 * 编译优化带来的有序性问题
 */
public class Singleton {
    /**
     分配一块内存 M；
     在内存 M 上初始化 Singleton 对象；
     然后 M 的地址赋值给 instance 变量。

     但是实际上优化后的执行路径却是这样的：

     分配一块内存 M；
     将 M 的地址赋值给 instance 变量；
     最后在内存 M 上初始化 Singleton 对象。
     
     volatile禁止指令重排
     */
    static volatile Singleton instance;

    /**
     优化后的执行路径却是这样的：

     分配一块内存 M；
     将 M 的地址赋值给 instance 变量；
     最后在内存 M 上初始化 Singleton 对象。
     优化后会导致什么问题呢？我们假设线程 A 先执行 getInstance() 方法，
     当执行完指令 2 时恰好发生了线程切换，切换到了线程 B 上；
     如果此时线程 B 也执行 getInstance() 方法，
     那么线程 B 在执行第一个判断时会发现 instance != null ，
     所以直接返回 instance，而此时的 instance 是没有初始化过的，
     如果我们这个时候访问 instance 的成员变量就可能触发空指针异常。
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

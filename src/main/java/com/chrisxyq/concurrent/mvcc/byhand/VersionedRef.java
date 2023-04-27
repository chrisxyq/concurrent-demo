package com.chrisxyq.concurrent.mvcc.byhand;

/** 带版本号的对象引用
 * VersionedRef 这个类的作用就是将对象 value 包装成带版本号的对象
 * @param <T>
 */
public final class VersionedRef<T> {
    final T value;
    final long version;
    // 构造方法
    public VersionedRef(T value, long version) {
        this.value = value;
        this.version = version;
    }
}

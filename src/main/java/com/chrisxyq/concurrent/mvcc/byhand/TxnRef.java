package com.chrisxyq.concurrent.mvcc.byhand;


/**
 * 支持事务的引用
 * 所有对数据的读写操作，一定是在一个事务里面，
 * TxnRef 这个类负责完成事务内的读写操作，读写操作委托给了接口 Txn
 * @param <T>
 */
public class TxnRef<T> {
    /**
     * 当前数据，带版本号
     */
    volatile VersionedRef curRef;
    // 构造方法
    public TxnRef(T value) {
        this.curRef = new VersionedRef(value, 0L);
    }

    /**获取当前事务中的数据
     * Txn 代表的是读写操作所在的当前事务， 内部持有的 curRef 代表的是系统中的最新值。
     * @param txn
     * @return
     */
    public T getValue(Txn txn) {
        return txn.get(this);
    }

    /**
     * 在当前事务中设置数据
     * @param value
     * @param txn
     */
    public void setValue(T value, Txn txn) {
        txn.set(this, value);
    }
}

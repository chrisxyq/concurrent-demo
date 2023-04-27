package com.chrisxyq.concurrent.mvcc.byhand;

/**
 * 读写操作委托给了接口 Txn，Txn 代表的是读写操作所在的当前事务
 */
public interface Txn {
    <T> T get(TxnRef<T> ref);
    <T> void set(TxnRef<T> ref, T value);
}

package com.chrisxyq.concurrent.mvcc.byhand;

@FunctionalInterface
public interface TxnRunnable {
    void run(Txn txn);
}
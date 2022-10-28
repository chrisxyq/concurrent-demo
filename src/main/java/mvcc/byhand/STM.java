package mvcc.byhand;

/**
 * 软件事务内存（Software Transactional Memory，简称 STM）
 * 基于 MVCC （全称是 Multi-Version Concurrency Control）实现一个简版的 STM
 *
 */
public final class STM {
    // 提交数据需要用到的全局锁
    static final Object commitLock = new Object();

    // 私有化构造方法
    private STM() {
    }

    /**
     * 原子化提交方法
     * 模拟实现 Multiverse 中的原子化操作 atomic()。
     * atomic() 方法中使用了类似于 CAS 的操作，
     * 如果事务提交失败，那么就重新创建一个新的事务，重新执行。
     *
     * @param action
     */
    public static void atomic(TxnRunnable action) {
        boolean committed = false;
        // 如果没有提交成功，则一直重试
        while (!committed) {
            // 创建新的事务
            STMTxn txn = new STMTxn();
            // 执行业务逻辑
            action.run(txn);
            // 提交事务
            committed = txn.commit();
        }
    }
}

package atomic.cas;

public class SimulatedCAS {
    volatile int count;

    /**
     * cas无锁实现 count+=1
     */
    void addOne() {
        int newValue;
        do {
            newValue = count + 1;
        } while (count != cas(count, newValue));
    }

    /**
     * 模拟实现 CAS，仅用来帮助理解
     * 此处使用synchronized锁实现原子性
     * 实际cas是使用硬件cpu原语实现的原子性
     *
     * @param expect
     * @param newValue
     * @return
     */
    synchronized int cas(
            int expect, int newValue) {
        // 读目前 count 的值
        int curValue = count;
        // 比较目前 count 值是否 == 期望值
        if (curValue == expect) {
            // 如果是，则更新 count 的值
            count = newValue;
        }
        // 返回写入前的值
        return curValue;
    }
}

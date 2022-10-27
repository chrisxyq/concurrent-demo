package pattern.balking;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Balking 模式本质上是一种规范化地解决“多线程版本的 if”的方案
 * 示例中的共享变量是一个状态变量，业务逻辑依赖于这个状态变量的状态：
 * 当状态满足某个条件时，执行某个业务逻辑
 */
class AutoSaveEditor {
    /**
     * 文件是否被修改过
     */
    boolean                  changed = false;
    /**
     * 定时任务线程池
     */
    ScheduledExecutorService ses     = Executors.newSingleThreadScheduledExecutor();

    /**
     * 定时执行自动保存
     */
    void startAutoSave() {
        ses.scheduleWithFixedDelay(() -> {
            autoSave();
        }, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * 自动存盘操作
     */
    void autoSave() {
        synchronized (this) {
            if (!changed) {
                return;
            }
            changed = false;
        }
        // 执行存盘操作
        // 省略且实现
        this.execSave();
    }

    private void execSave() {
    }

    /**
     * 编辑操作
     * edit() 方法中对共享变量 changed
     * 的赋值操作抽取到了 change() 中，
     * 这样的好处是将并发处理逻辑和业务逻辑分开。
     */
    void edit() {
        // 省略编辑逻辑
        change();
    }

    private void change() {
        synchronized (this) {
            changed = true;
        }
    }
}

public class AutoSaveEditorTest {
}

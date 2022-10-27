package nolock.copyonwrite;

import lombok.Data;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MINUTES;


/**
 * 路由表信息
 * 路由表是典型的读多写少类问题
 * 对读的性能要求很高，读多写少，弱一致性
 */
public class RouterTable {
    /**
     * Key: 接口名
     * Value: 路由集合
     */
    ConcurrentHashMap<String, CopyOnWriteArraySet<Router>>
            routerMap = new ConcurrentHashMap<>();
    /**
     * 路由表是否发生变化
     */
    volatile boolean changed;
    /**
     * 将路由表写入本地文件的线程池
     */
    ScheduledExecutorService executorService =
            Executors.newSingleThreadScheduledExecutor();

    /**
     * 启动定时任务
     * 将变更后的路由表写入本地文件
     */
    public void startLocalSaver(){
        executorService.scheduleWithFixedDelay(()->{
            autoSave();
        }, 1, 1, MINUTES);
    }

    /**
     * 保存路由表到本地文件
     */
    void autoSave() {
        if (!changed) {
            return;
        }
        changed = false;
        // 将路由表写入本地文件
        // 省略其方法实现
        this.save2Local();
    }

    private void save2Local() {
    }

    /**
     * 删除路由
     * @param router
     */
    public void remove(Router router) {
        Set<Router> set=routerMap.get(router.getIface());
        if (set != null) {
            set.remove(router);
            // 路由表已发生变化
            changed = true;
        }
    }

    /**
     * 增加路由
     * @param router
     */
    public void add(Router router) {
        Set<Router> set = routerMap.computeIfAbsent(
                router.getIface(), r ->
                        new CopyOnWriteArraySet<>());
        set.add(router);
        // 路由表已发生变化
        changed = true;
    }
    /**
     * 根据接口名获取路由表
     *
     * @param iface
     * @return
     */
    public Set<Router> get(String iface) {
        return routerMap.get(iface);
    }
}
/**
 * 路由信息
 */
@Data
final class Router {
    private final String  ip;
    private final Integer port;
    private final String  iface;

    // 构造函数
    public Router(String ip,
                  Integer port, String iface) {
        this.ip = ip;
        this.port = port;
        this.iface = iface;
    }

    // 重写 equals 方法
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Router) {
            Router r = (Router) obj;
            return iface.equals(r.iface) &&
                    ip.equals(r.ip) &&
                    port.equals(r.port);
        }
        return false;
    }

    @Override
    public int hashCode() {
        // 省略 hashCode 相关代码
        return 1;
    }
}
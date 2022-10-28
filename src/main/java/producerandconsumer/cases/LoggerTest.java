package producerandconsumer.cases;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.*;

@Slf4j
public class LoggerTest {
    /**
     * 可以通过调用 info()和error() 方法写入日志，
     * 这两个方法都是创建了一个日志任务 LogMsg，并添加到阻塞队列中，
     * 调用 info()和error() 方法的线程是生产者；
     * 而真正将日志写入文件的是消费者线程，
     * 在 Logger 这个类中，我们只创建了 1 个消费者线程，
     * 在这个消费者线程中，会根据刷盘规则执行刷盘操作
     */
    @Test
    public void test() throws InterruptedException, IOException {
        Logger logger = new Logger();
        logger.info("Logger.info");
        logger.error("Logger.error");
        Thread thread = new Thread(() -> {
            try {
                logger.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
    }
}
class Logger {
    /**
     * 用于终止日志执行的“毒丸”
     */
    final LogMsg poisonPill = new LogMsg(LEVEL.ERROR, "");
    /**
     * 任务队列
     */
    volatile     BlockingQueue<LogMsg> bq        = new LinkedBlockingQueue<>();
    /**
     * flush 批量
     */
    static final int                   batchSize = 500;
    /**
     * 只需要一个线程写日志
     */
    ExecutorService es = Executors.newFixedThreadPool(1);

    /**
     * 启动写日志线程
     * @throws IOException
     */
    void start() throws IOException {
        File file = File.createTempFile("foo", ".log");
        final FileWriter writer = new FileWriter(file);
        this.es.execute(() -> {
            try {
                // 未刷盘日志数量
                int curIdx = 0;
                long preFT = System.currentTimeMillis();
                while (true) {
                    LogMsg log = bq.poll(5, TimeUnit.SECONDS);
                    // 如果是“毒丸”，终止执行
                    if(poisonPill.equals(log)){
                        break;
                    }
                    // 写日志
                    if (log != null) {
                        writer.write(log.toString());
                        ++curIdx;
                    }
                    // 如果不存在未刷盘数据，则无需刷盘
                    if (curIdx <= 0) {
                        continue;
                    }
                    /**
                     * ERROR 级别的日志需要立即刷盘；
                     * 数据积累到 500 条需要立即刷盘；
                     * 存在未刷盘数据，且 5 秒钟内未曾刷盘，需要立即刷盘。
                     */
                    if (log != null && log.level == LEVEL.ERROR ||
                            curIdx == batchSize || System.currentTimeMillis() - preFT > 5000) {
                        writer.flush();
                        curIdx = 0;
                        preFT = System.currentTimeMillis();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 写 INFO 级别日志
     * @param msg
     * @throws InterruptedException
     */
    void info(String msg) throws InterruptedException {
        bq.put(new LogMsg(LEVEL.INFO, msg));
    }

    /**
     * 写 ERROR 级别日志
     * @param msg
     * @throws InterruptedException
     */
    void error(String msg) throws InterruptedException {
        bq.put(new LogMsg(LEVEL.ERROR, msg));
    }

    /**
     * 终止写日志线程
     */
    public void stop() {
        // 将“毒丸”对象加入阻塞队列
        bq.add(poisonPill);
        es.shutdown();
    }
}

/**
 * 日志级别
 */
enum LEVEL {
    INFO, ERROR
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class LogMsg {
    LEVEL  level;
    String msg;


    @Override
    public String toString() {
        return "LogMsg{" +
                "level=" + level +
                ", msg='" + msg + '\'' +
                '}';
    }
}
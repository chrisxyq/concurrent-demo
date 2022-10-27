package forkjoin;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

//MR 模拟类
class Task extends RecursiveTask<Map<String, Long>> {
    private String[] words;
    private int      start, end;

    // 构造函数
    Task(String[] words, int start, int end) {
        this.words = words;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Map<String, Long> compute() {
        if (end - start == 1) {
            return calc(words[start]);
        } else {
            int mid = (start + end) / 2;
            Task task1 = new Task(words, start, mid);
            task1.fork();
            Task task2 = new Task(words, mid, end);
            // 计算子任务，并返回合并的结果
            return merge(task2.compute(), task1.join());
        }
    }

    /**
     * 合并结果
     * @param r1
     * @param r2
     * @return
     */
    private Map<String, Long> merge(Map<String, Long> r1, Map<String, Long> r2) {
        Map<String, Long> result = new HashMap<>();
        result.putAll(r1);
        // 合并结果
        r2.forEach((k, v) -> {
            Long c = result.get(k);
            if (c != null)
                result.put(k, c + v);
            else
                result.put(k, v);
        });
        return result;
    }

    /**
     * 统计单词数量
     * @param line
     * @return
     */
    private Map<String, Long> calc(String line) {
        Map<String, Long> result = new HashMap<>();
        // 分割单词
        String[] words = line.split("\\s+");
        // 统计单词数量
        for (String w : words) {
            Long v = result.get(w);
            if (v != null)
                result.put(w, v + 1);
            else
                result.put(w, 1L);
        }
        return result;
    }
}

/**
 * 默认情况下所有的并行流计算都共享一个 ForkJoinPool，
 * 这个共享的 ForkJoinPool 默认的线程数是 CPU 的核数；
 * 如果所有的并行流计算都是 CPU 密集型计算的话，
 * 完全没有问题，但是如果存在 I/O 密集型的并行流计算，
 * 那么很可能会因为一个很慢的 I/O 计算而拖慢整个系统的性能。
 */
public class ForkJoinTest {
    ForkJoinPool pool = new ForkJoinPool(3);
    @Test
    public void test() {
        String[] words = {"hello world",
                "hello me",
                "hello fork",
                "hello join",
                "fork join in world"};
        // 创建任务
        Task mr = new Task(words, 0, words.length);
        // 启动任务
        Map<String, Long> result = pool.invoke(mr);
        // 输出结果
        result.forEach((k, v) ->
                System.out.println(k + ":" + v));
    }

}

package forkJoin;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.LongStream;

/**
 * 并行流背后使用的基础框架就是 Java7 中引入的分支/合并框架。
 *
 * Fork/Join（分支/合并）框架的目的是以递归方式将可以并行的任务拆分(fork)成更小的任务，
 * 然后将每个任务的结果合并 (join)起来生成整体效果。它是ExectorService接口的一个实现，把子任务分配给线程池（称为ForkJoinPool）中的工作线程。
 */
public class ForkJoinSumCalculator extends RecursiveTask<Long> {

    private final long[] numbers;
    private final int start;
    private final int end;

    //不再将任务分解为子任务的数组大小
    public static long THRESHOLD = 100;

    //公共构造器用于创建主任务
    public ForkJoinSumCalculator(long[] numbers) {
        this(numbers, 0, numbers.length);
    }

    //私有构造器用于以递归方式为主任务创建子任务
    private ForkJoinSumCalculator(long[] numbers, int start, int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        //如果大小小于等于阈值，顺序计算结果
        if (length <= THRESHOLD) {
            return computerSequntially();
        }

        ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(numbers, start, start + length / 2);

        leftTask.fork();

        ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(numbers, start + length / 2, end);

        Long rightResult = rightTask.compute();   //同步执行第二个任务，
        Long leftResult = leftTask.join(); // 读取第一个子任务的结果，如果尚未完成就等待
        return rightResult + leftResult;
    }


    // 子任务不再可分时计算和
    private long computerSequntially() {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        return sum;
    }

    public static long forkJoimSum(long n) {
        long[] numbers = LongStream.rangeClosed(1, n).toArray();
        ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers);
        return new ForkJoinPool().invoke(task);
    }

    public static void main(String[] args) {
        System.out.println("sum:" + forkJoimSum(10000));
    }
}
package cas;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class CASTest {
    @Test
    public void compareAndSet(){
        AtomicInteger atomicInteger = new AtomicInteger(5);

        System.out.println(atomicInteger.compareAndSet(5, 2019)+"\t current data:"+atomicInteger.get());
        System.out.println(atomicInteger.compareAndSet(5, 1024)+"\t current data:"+atomicInteger.get());
        atomicInteger.getAndIncrement();
    }

}

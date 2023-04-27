package com.chrisxyq.concurrent.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 我们启动了 4 个线程来执行累加操作。整个程序没有锁，也没有 CAS，但是程序是线程安全的。
 * Actor 中的消息机制，就可以类比这现实世界里的写信。Actor 内部有一个邮箱（Mailbox），
 * 接收到的消息都是先放到邮箱里，如果邮箱里有积压的消息，
 * 那么新收到的消息就不会马上得到处理，
 * 也正是因为 Actor 使用单线程处理消息，所以不会出现并发问题。
 * 你可以把 Actor 内部的工作模式想象成只有一个消费者线程的生产者 - 消费者模式。
 */
class CounterActor extends UntypedActor {
    private int counter = 0;

    @Override
    public void onReceive(Object message) {
        // 如果接收到的消息是数字类型，执行累加操作，
        // 否则打印 counter 的值
        if (message instanceof Number) {
            counter += ((Number) message).intValue();
        } else {
            System.out.println(counter);
        }
    }
}

public class ActorTest {
    //4 个线程生产消息
    ExecutorService es = Executors.newFixedThreadPool(4);

    @Test
    public void test() throws InterruptedException {
        // 创建 Actor 系统
        ActorSystem system = ActorSystem.create("HelloSystem");
        // 创建 CounterActor
        ActorRef counterActor = system.actorOf(Props.create(CounterActor.class));
        // 生产 4*100000 个消息
        for (int i = 0; i < 4; i++) {
            es.execute(() -> {
                for (int j = 0; j < 100000; j++) {
                    counterActor.tell(1, ActorRef.noSender());
                }
            });
        }
        // 关闭线程池
        es.shutdown();
        // 等待 CounterActor 处理完所有消息
        Thread.sleep(1000);
        // 打印结果
        counterActor.tell("", ActorRef.noSender());
        // 关闭 Actor 系统
        system.shutdown();
    }
}

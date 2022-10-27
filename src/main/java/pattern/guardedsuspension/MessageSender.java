package pattern.guardedsuspension;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Objects;


@Slf4j
public class MessageSender {
    /**
     * wtf????
     */
    @Test
    public void test() {
        // MessageSender sender = new MessageSender();
        //sender.handleWebReq();
        //
        //
        ////
        //int id = "UUID".hashCode();
        // GuardedObject guardedObject = GuardedObject.create(id);
        //Message msg = new Message(id, "{...}");
        //sender.onMessage( msg);
    }

    /**
     * 处理浏览器发来的请求
     *
     * @return
     */
    Response handleWebReq() {
        int id = "UUID".hashCode();
        // 创建一消息
        Message msg = new Message(id, "{...}");
        // 创建 GuardedObject 实例
        GuardedObject<Message> guardedObject = GuardedObject.create(id);
        // 发送消息
        send(msg);
        // 等待 MQ 消息
        Message r = guardedObject.get(Objects::nonNull);
        return null;
    }

    private void send(Message msg) {
    }

    void onMessage(Message msg) {
        // 唤醒等待的线程
        GuardedObject.fireEvent(msg.id, msg);
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Message {
    int    id;
    String content;
}

class Response {
}
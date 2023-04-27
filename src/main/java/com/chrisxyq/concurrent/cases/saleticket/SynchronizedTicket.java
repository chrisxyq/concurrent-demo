package com.chrisxyq.concurrent.cases.saleticket;

/**
 * @author chrisxu
 * @create 2021-10-03 18:12
 * Ctrl + Alt + L：格式化代码
 * ctrl + Alt + T：代码块包围
 * ctrl + Y：删除行
 * ctrl + D：复制行
 * alt+上/下：移动光标到上/下方法
 * ctrl+shift+/：注释多行
 */
public class SynchronizedTicket {
    private int number = 30;

    public synchronized void sale() {
        if (number > 0) {
            System.out.println(Thread.currentThread().getName() + ":卖出：" + number-- + "剩下：" + number);
        }
    }
}

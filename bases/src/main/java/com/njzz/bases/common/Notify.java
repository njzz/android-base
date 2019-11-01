package com.njzz.bases.common;

import java.util.List;

/**
 * 通用通知器
 */
public class Notify {

    //多个通知
    public static void AsyncSend(final List<Receiver> receiver,final MessageSet ms){
        if(receiver !=null) {
            new Thread(() -> Send(receiver, ms)).start();
        }
    }
    //单个通知
    public static void AsyncSend(final Receiver receiver,final MessageSet ms){
        if(receiver !=null) {
            new Thread(() -> Send(receiver, ms)).start();
        }
    }

    //多个通知
    public static void Send(final List<Receiver> receiver,final MessageSet ms){
        if(receiver !=null){
            for(Receiver n: receiver){
                Send(n,ms);
            }
        }
    }
    //单个通知
    public static void Send(final Receiver receiver,final MessageSet ms){
        if(receiver !=null) {
            receiver.action(ms);
        }
    }
}

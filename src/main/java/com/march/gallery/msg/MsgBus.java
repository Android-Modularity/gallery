package com.march.gallery.msg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * CreateAt : 2018/8/1
 * Describe :
 *
 * @author chendong
 */
public class MsgBus {

    private static MsgBus sInst;

    public static MsgBus getInst() {
        if (sInst == null) {
            synchronized (MsgBus.class) {
                if (sInst == null) {
                    sInst = new MsgBus();
                }
            }
        }
        return sInst;
    }


    private MsgBus() {
        subscribers = new HashSet<>();
    }

    interface SubscriberInvoker {
        void invoke(Object data);
    }

    static class Subscriber {

        private String            key;
        private SubscriberInvoker invoker;

        public Subscriber(String key, SubscriberInvoker invoker) {
            this.key = key;
            this.invoker = invoker;
        }
    }

    private Set<Subscriber> subscribers;


    public void register(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void register(String key, SubscriberInvoker invoker) {
        subscribers.add(new Subscriber(key, invoker));
    }

    public void unRegister(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public void unRegister(String key) {
        synchronized (MsgBus.class) {
            Iterator<Subscriber> iterator = subscribers.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().key.equals(key)) {
                    iterator.remove();
                }
            }
        }
    }

    public void post(String key, Object data) {
        for (Subscriber subscriber : subscribers) {
            if (key.equals(subscriber.key)) {
                subscriber.invoker.invoke(data);
            }
        }
    }
}

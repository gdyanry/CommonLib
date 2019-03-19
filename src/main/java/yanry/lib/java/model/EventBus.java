package yanry.lib.java.model;

import yanry.lib.java.model.log.Logger;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Administrator
 */
public class EventBus {
    private List<Class<?>> interfaces;
    private Map<Method, Class<?>> method_interface;
    private Map<Class<?>, Method> event_method;
    private Map<Class<?>, List<Object>> interface_subscriber;
    private List<Object> persistEvents;

    public EventBus() {
        interfaces = new LinkedList<>();
        method_interface = new HashMap<>();
        event_method = new HashMap<>();
        interface_subscriber = new HashMap<>();
        persistEvents = new LinkedList<>();
    }

    public EventBus addSubscriberInterface(Class<?> i) {
        if (!interfaces.contains(i)) {
            interfaces.add(i);
            Method[] methods = i.getMethods();
            for (Method m : methods) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length == 1) {
                    method_interface.put(m, i);
                    event_method.put(params[0], m);
                }
            }
        }
        return this;
    }

    public void register(Object subscriber) throws Exception {
        for (Class<?> i : subscriber.getClass().getInterfaces()) {
            if (interfaces.contains(i)) {
                List<Object> subscribers = interface_subscriber.get(i);
                if (subscribers == null) {
                    subscribers = new LinkedList<>();
                    interface_subscriber.put(i, subscribers);
                }
                if (!subscribers.contains(subscriber)) {
                    subscribers.add(subscriber);
                    // consume persist events
                    Iterator<Object> it = persistEvents.iterator();
                    while (it.hasNext()) {
                        Object event = it.next();
                        Method m = event_method.get(event.getClass());
                        if (i.equals(method_interface.get(m))) {
                            it.remove();
                            m.invoke(subscriber, event);
                        }
                    }
                } else {
                    Logger.getDefault().e("register interface %s multiple times on subscriber %s.", i.getSimpleName(), subscriber.getClass().getSimpleName());
                }
            }
        }
    }

    public void unregister(Object subscriber) {
        for (Class<?> c : subscriber.getClass().getInterfaces()) {
            if (interfaces.contains(c)) {
                List<Object> subscribers = interface_subscriber.get(c);
                if (!subscribers.remove(subscriber)) {
                    Logger.getDefault().e("subscriber %s is not registered yet.", subscriber.getClass().getSimpleName());
                }
            }
        }
    }

    public void post(Object event) throws Exception {
        post(event, EventType.Transient);
    }

    public void post(Object event, EventType type) throws Exception {
        Method m = event_method.get(event.getClass());
        boolean consumed = false;
        if (m != null) {
            List<Object> subscribers = interface_subscriber.get(method_interface.get(m));
            if (subscribers != null) {
                for (Object s : subscribers) {
                    m.invoke(s, event);
                    consumed = true;
                }
            }
        }
        if (type != EventType.Transient && !consumed) {
            // need persist
            if (type == EventType.SinglePersist) {
                Iterator<Object> it = persistEvents.iterator();
                while (it.hasNext()) {
                    Object e = it.next();
                    if (e.getClass().equals(event.getClass())) {
                        it.remove();
                    }
                }
            }
            persistEvents.add(event);
        }
    }

    public enum EventType {
        /**
         * default type, event will be dropped whether it's consumed or not.
         */
        Transient,

        /**
         * event will persist if it's not consumed immediately.
         */
        Persist,

        /**
         * event will persist and previous persisting events with the same type
         * will be dropped if it's not consumed immediately.
         */
        SinglePersist
    }
}

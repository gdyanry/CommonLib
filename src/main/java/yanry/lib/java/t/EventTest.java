package yanry.lib.java.t;

import yanry.lib.java.model.event.Event;
import yanry.lib.java.model.event.EventDispatcher;
import yanry.lib.java.model.event.EventInterceptor;
import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;

public class EventTest {

    public static void main(String[] args) {
        Test.setupLogger();
        EventDispatcher<Event, EventInterceptor<Event>> topDispatcher = new EventDispatcher<Event, EventInterceptor<Event>>() {
            @Override
            public String toString() {
                return "top";
            }
        };

        EventDispatcher<Event, EventInterceptor<Event>> subDispatcher = new EventDispatcher<Event, EventInterceptor<Event>>() {
            @Override
            public String toString() {
                return "sub";
            }
        };
        subDispatcher.register(new EventInterceptor<Event>() {
            private int count;

            @Override
            public int onDispatchEvent(Event event) {
                if (++count < 3) {
                    Logger.getDefault().ii(this, " onDispatchEvent<: ", event.getCurrentLevel());
                    Event secondEvent = new Event() {
                        private String name = String.valueOf((char) ('A' + count));

                        @Override
                        public String toString() {
                            return name;
                        }
                    };
                    secondEvent.configLogger(Logger.getDefault(), LogLevel.Debug);
                    Logger.getDefault().dd("dispatch second event: ", secondEvent);
                    topDispatcher.dispatchEvent(secondEvent);
                }
                Logger.getDefault().ii(this, " onDispatchEvent>: ", event.getCurrentLevel());
                return 0;
            }

            @Override
            public int onEvent(Event event) {
                Logger.getDefault().ww(this, " onEvent: ", event.getCurrentLevel());
                return 0;
            }

            @Override
            public String toString() {
                return "subInterceptor";
            }
        });
        topDispatcher.register(subDispatcher);
        topDispatcher.register(new EventInterceptor<Event>() {

            @Override
            public int onDispatchEvent(Event event) {
                Logger.getDefault().ii(this, " onDispatchEvent>: ", event.getCurrentLevel());
                return 0;
            }

            @Override
            public int onEvent(Event event) {
                Logger.getDefault().ww(this, " onEvent: ", event.getCurrentLevel());
                return 0;
            }

            @Override
            public String toString() {
                return "topInterceptor";
            }
        });
        Event firstEvent = new Event() {
            @Override
            public String toString() {
                return "A";
            }
        };
        firstEvent.configLogger(Logger.getDefault(), LogLevel.Debug);
        Logger.getDefault().dd("dispatch first event: ", firstEvent);
        topDispatcher.dispatchEvent(firstEvent);
    }
}
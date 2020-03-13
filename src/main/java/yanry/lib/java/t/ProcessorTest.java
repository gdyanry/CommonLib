package yanry.lib.java.t;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import yanry.lib.java.model.Singletons;
import yanry.lib.java.model.log.LogLevel;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.log.extend.ConsoleHandler;
import yanry.lib.java.model.log.extend.SimpleFormatter;
import yanry.lib.java.model.process.ProcessCallback;
import yanry.lib.java.model.process.Processor;
import yanry.lib.java.model.process.RequestHook;
import yanry.lib.java.model.process.extend.PlainProcessor;

/**
 * Created by yanry on 2020/1/11.
 */
public class ProcessorTest {
    private static final int MAX_TIMEOUT = 10000;
    private static final int FACTOR = 3;
    private static Executor executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        ConsoleHandler defaultHandler = new ConsoleHandler();
        SimpleFormatter formatter = new SimpleFormatter();
        formatter.addFlag(SimpleFormatter.LEVEL).addFlag(SimpleFormatter.TIME).addFlag(SimpleFormatter.SEQUENCE_NUMBER).addFlag(SimpleFormatter.THREAD).addFlag(SimpleFormatter.METHOD);
        formatter.setMethodStack(0);
        defaultHandler.setFormatter(formatter);
        defaultHandler.setLevel(LogLevel.Verbose);
        Logger.setDefaultHandler(defaultHandler);

        ProcessCallback<String> completeCallback = new ProcessCallback<String>() {
            @Override
            public void onSuccess(String result) {
                System.out.println("success");
                Runtime.getRuntime().exit(0);
            }

            @Override
            public void onFail(boolean isTimeout) {
                System.out.println("timeout: " + isTimeout);
//                new RootProcessor(FACTOR, false).request(Logger.getDefault(), 10086, this);
            }
        };
        new RootProcessor(FACTOR, false).request(Logger.getDefault(), 2, completeCallback);
    }

    private static class NodeProcessor extends PlainProcessor<String, String> {
        private static AtomicInteger counter = new AtomicInteger();
        private int index;
        private boolean hit;

        NodeProcessor(boolean hit) {
            this.hit = hit;
            index = counter.getAndIncrement();
        }

        @Override
        public long getTimeout() {
            return Singletons.get(Random.class).nextInt(MAX_TIMEOUT);
//            return 0;
        }

        @Override
        protected String process(String requestData) {
            try {
                int sleep = Singletons.get(Random.class).nextInt(MAX_TIMEOUT);
                System.out.println(String.format("%s sleep: %s", getShortName(), sleep));
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return hit ? "hit" : null;
        }

        @Override
        protected Executor getExecutor() {
            return executor;
        }

        @Override
        public String getShortName() {
            return (hit ? "Hit" : "Fail") + "Processor" + index;
        }
    }

    private static class Dispatcher implements Processor<Integer, String> {
        private static AtomicInteger counter = new AtomicInteger();
        private ArrayList<Processor<Integer, String>> childProcessors;
        private boolean keepOrder;
        private int index;

        public Dispatcher(int childCount) {
            childProcessors = new ArrayList<>(childCount);
            this.keepOrder = Singletons.get(Random.class).nextBoolean();
            index = counter.getAndIncrement();
            int hitIndex = Singletons.get(Random.class).nextInt(childCount);
            for (int i = 0; i < childCount; i++) {
                childProcessors.add(new NodeProcessor(i == hitIndex).wrap(input -> "str:" + input));
            }
        }

        @Override
        public void process(RequestHook<Integer, String> request) {
            request.dispatch(request.getRequestData(), childProcessors, keepOrder);
        }

        @Override
        public long getTimeout() {
            return Singletons.get(Random.class).nextInt(MAX_TIMEOUT);
        }

        @Override
        public String getShortName() {
            return "Dispatcher" + index + keepOrder;
        }
    }

    private static class RootProcessor implements Processor<Integer, String> {
        private ArrayList<Processor<Integer, String>> childProcessors;
        private boolean keepOrder;

        public RootProcessor(int childCount, boolean keepOrder) {
            this.keepOrder = keepOrder;
            childProcessors = new ArrayList<>(childCount);
            for (int i = 0; i < childCount; i++) {
                childProcessors.add(new Dispatcher(FACTOR));
            }
        }

        @Override
        public void process(RequestHook<Integer, String> request) {
            request.dispatch(request.getRequestData(), childProcessors, keepOrder);
        }

        @Override
        public long getTimeout() {
            return Singletons.get(Random.class).nextInt(MAX_TIMEOUT);
        }
    }
}

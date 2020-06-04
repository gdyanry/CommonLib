package yanry.lib.java.t;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.log.extend.ConsoleHandler;
import yanry.lib.java.model.log.extend.SimpleFormatter;
import yanry.lib.java.model.mock.MockAsMap;

public class MockTest extends MockAsMap {
    private Executor executor;

    public MockTest() {
        super("e:/aa/alarm.txt", "utf-8", System.lineSeparator(), "=");
        executor = Executors.newCachedThreadPool();
    }

    public static void main(String... args) throws IOException {
        SimpleFormatter formatter = new SimpleFormatter();
        formatter.addFlag(SimpleFormatter.THREAD).addFlag(SimpleFormatter.SEQUENCE_NUMBER);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        Logger.getDefault().addHandler(handler);
        MockTest mockTest = new MockTest();
        mockTest.monitor();
        mockTest.loadFile();
    }

    @Override
    protected void onLoadComplete(HashMap<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Logger.getDefault().dd(entry.getKey(), '=', entry.getValue());
        }
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }
}

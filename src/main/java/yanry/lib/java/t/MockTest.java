package yanry.lib.java.t;

import yanry.lib.java.model.log.ConsoleHandler;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.log.SimpleFormatter;
import yanry.lib.java.model.mock.MockAsMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MockTest extends MockAsMap {
    private Executor executor;

    public MockTest() {
        super("e:/aa/alarm.txt", "utf-8", System.lineSeparator(), "=");
        executor = Executors.newCachedThreadPool();
    }

    public static void main(String... args) throws IOException {
        Logger.getDefault().addHandler(new ConsoleHandler(new SimpleFormatter().thread().sequenceNumber(), null));
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
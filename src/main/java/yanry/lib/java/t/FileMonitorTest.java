package yanry.lib.java.t;

import yanry.lib.java.model.FileMonitor;
import yanry.lib.java.model.log.ConsoleHandler;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.log.SimpleFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

public class FileMonitorTest {
    public static void main(String[] args) {
        // try {
        // WatchService wws = FileSystems.getDefault().newWatchService();
        // Path p = new File("e:/aa").toPath();
        // p.register(wws, StandardWatchEventKinds.ENTRY_CREATE,
        // StandardWatchEventKinds.ENTRY_DELETE,
        // StandardWatchEventKinds.ENTRY_MODIFY,
        // StandardWatchEventKinds.OVERFLOW);
        // while (true) {
        // WatchKey key = wws.take();
        // for (WatchEvent<?> e : key.pollEvents()) {
        // System.out.println(String.format("context: %s%ncount: %s%nkind-name:
        // %s%n", e.context(), e.count(),
        // e.kind().name()));
        // }
        // key.reset();
        // }
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        Logger.getDefault().addHandler(new ConsoleHandler(new SimpleFormatter().thread(), null));
        try {
            FileMonitor fcm = new FileMonitor();
            fcm.monitor(new FileMonitor.WatchItem(new File("e:/aa/cc.txt"), StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.OVERFLOW) {

                @Override
                protected void onEvent(WatchEvent<?> e) {
                    Logger.getDefault().dd(e.kind(), ' ', e.context(), ' ', e.count());
                }
            });
            new Thread(fcm).start();
            Thread.sleep(6000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

package yanry.lib.java.model;

import yanry.lib.java.model.log.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.util.*;

/**
 * This is a wrapper class of {@link WatchService} and the monitored item can be
 * precise to a single file. Note that it's also an implementation of
 * {@link Runnable}, so remember to post it to a thread to make it work.
 *
 * @author yanry
 * <p>
 * 2015年1月6日 下午3:08:28
 */
public class FileMonitor implements Runnable {

    private WatchService ws;
    private Map<Path, Set<WatchItem>> watchItems;
    private boolean exit;

    /**
     * @throws IOException see {@link FileSystem #newWatchService()}.
     */
    public FileMonitor() throws IOException {
        ws = FileSystems.getDefault().newWatchService();
        watchItems = new HashMap<>();
    }

    /**
     * For directory, all events are related to its children! Create and delete
     * event of an item will cause modify event of its parent folder, and this
     * modify event can be triggered on current folder's parent folder; imagine file system
     * hierarchy as linked list structure may help understand this. Monitor the
     * same item more than once is allowed and has little performance effect.
     *
     * @param item
     * @throws IOException see {@link Path #register(WatchService, Kind...)}
     */
    public void monitor(WatchItem item) throws IOException {
        if (item.p == null) {
            Logger.getDefault().ee(item.f.getAbsoluteFile(), " does not exist!");
            return;
        }
        Logger.getDefault().dd("monitor: ", item);
        Set<WatchItem> items = watchItems.get(item.p);
        if (items == null) {
            items = new HashSet<>();
            watchItems.put(item.p, items);
            Kind<?>[] kinds = item.kinds;
            item.p.register(ws, kinds);
            Logger.getDefault().d("register %s %s", item.p, Arrays.toString(kinds));
        } else {
            // register same path will always return same key and override kinds
            // with the new one!
            Set<Kind<?>> kinds = getKinds(items);
            if (kinds.addAll(Arrays.asList(item.kinds))) {
                // register again with wider range
                reRegister(item.p, kinds);
            }
        }
        items.add(item);
    }

    private void reRegister(Path p, Set<Kind<?>> kinds) throws IOException {
        Kind<?>[] ks = new Kind<?>[kinds.size()];
        int i = 0;
        for (Kind<?> k : kinds) {
            ks[i++] = k;
        }
        p.register(ws, ks);
        Logger.getDefault().d("re-register %s %s", p, Arrays.toString(ks));
    }

    private Set<Kind<?>> getKinds(Set<WatchItem> items) {
        Set<Kind<?>> kinds = new HashSet<Kind<?>>();
        for (WatchItem wi : items) {
            kinds.addAll(Arrays.asList(wi.kinds));
        }
        return kinds;
    }

    /**
     * @param item this watch item doesn't have to be monitored before, for
     *             example, a file whose folder have been monitored can be
     *             unmonitored alone.
     * @throws IOException
     */
    public void unmonitor(WatchItem item) throws IOException {
        if (item.p != null) {
            Set<WatchItem> items = watchItems.get(item.p);
            if (items != null) {
                Set<Kind<?>> before = new HashSet<WatchEvent.Kind<?>>();
                before.addAll(getKinds(items));
                if (items.remove(item)) {
                    Logger.getDefault().dd("un-monitor: ", item);
                    Set<Kind<?>> after = getKinds(items);
                    if (!after.containsAll(before)) {
                        // kinds have been decreased
                        reRegister(item.p, after);
                    }
                }
            }
        }
    }

    /**
     * @throws IOException see @{@link WatchService #close()}.
     */
    public void exit() throws IOException {
        exit = true;
        ws.close();
    }

    @Override
    public void run() {
        List<WatchItem> temp = new LinkedList<FileMonitor.WatchItem>();
        while (!exit) {
            try {
                WatchKey key = ws.take();
                Set<WatchItem> items = watchItems.get(key.watchable());
                if (items != null) {
                    temp.clear();
                    temp.addAll(items);
                    List<WatchEvent<?>> pollEvents = key.pollEvents();
                    for (WatchEvent<?> e : pollEvents) {
                        for (WatchItem item : temp) {
                            // in case of watching a non-directory file and the
                            // watch event is not related to that file..
                            if (item.f.getName().equals(e.context().toString()) || item.f.toPath().equals(key.watchable())) {
                                for (Kind<?> k : item.kinds) {
                                    if (k == e.kind() && (item.f.exists() || k.equals(StandardWatchEventKinds.ENTRY_DELETE))) {
                                        Logger.getDefault().vv("event: ", key.watchable(), ' ', e.kind().name());
                                        item.onEvent(e);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                key.reset();
            } catch (ClosedWatchServiceException e) {
                Logger.getDefault().dd("exit.");
            } catch (Exception e) {
                Logger.getDefault().catches(e);
            }
        }
    }

    public static abstract class WatchItem {
        private Kind<?>[] kinds;
        private File f;
        private Path p;

        /**
         * @param file       file or directory to monitor.
         * @param eventKinds use {@link Kind} constants of {@link StandardWatchEventKinds}
         */
        public WatchItem(File file, Kind<?>... eventKinds) {
            kinds = eventKinds;
            f = file;
            p = f.exists() ? (f.isFile() ? f.getParentFile() : f).toPath() : null;
        }

        /**
         * format: file_or_directory_name [kind, ...]
         */
        @Override
        public String toString() {
            return String.format("%s %s", f.getName(), Arrays.toString(kinds));
        }

        protected abstract void onEvent(WatchEvent<?> e) throws Exception;
    }
}

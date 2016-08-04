/**
 * 
 */
package lib.common.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lib.common.util.ConsoleUtil;

/**
 * This is a wrapper class of {@link WatchService} and the monitored item can be
 * precise to a single file. Note that it's also an implementation of
 * {@link Runnable}, so remember to post it to a thread to make it work.
 * 
 * @author yanry
 *
 *         2015年1月6日 下午3:08:28
 */
public class FileMonitor implements Runnable {

	private WatchService ws;
	private Map<Path, Set<WatchItem>> watchItems;
	private boolean exit;

	/**
	 * 
	 * @throws IOException
	 *             see {@link FileSystem #newWatchService()}.
	 */
	public FileMonitor() throws IOException {
		ws = FileSystems.getDefault().newWatchService();
		watchItems = new HashMap<Path, Set<WatchItem>>();
	}

	/**
	 * For directory, all events are related to its children! Create and delete
	 * event of an item will cause modify event of its parent folder, and this
	 * modify event can be trigger on folder' parent folder; imagine file system
	 * hierarchy as linked list structure may help understand this. Monitor the
	 * same item more than once is allowed and has little performance effect.
	 * 
	 * @param item
	 * @throws IOException
	 *             see {@link Path #register(WatchService, Kind...)}
	 */
	public void monitor(WatchItem item) throws IOException {
		if (item.p == null) {
			ConsoleUtil.error(getClass(), String.format("%s does not exist!", item.f.getAbsoluteFile()));
			return;
		}
		ConsoleUtil.debug(getClass(), "monitor: " + item);
		Set<WatchItem> items = watchItems.get(item.p);
		if (items == null) {
			items = new HashSet<FileMonitor.WatchItem>();
			watchItems.put(item.p, items);
			Kind<?>[] kinds = item.kinds;
			item.p.register(ws, kinds);
			ConsoleUtil.debug(getClass(), String.format("register %s %s", item.p, Arrays.toString(kinds)));
		} else {
			// register same path will always return same key and override kinds
			// with the new one!
			Set<Kind<?>> kinds = getKinds(items);
			if (kinds.addAll(Arrays.asList(item.kinds))) {
				// register again with wider range
				reregister(item.p, kinds);
			}
		}
		items.add(item);
	}

	private void reregister(Path p, Set<Kind<?>> kinds) throws IOException {
		Kind<?>[] ks = new Kind<?>[kinds.size()];
		int i = 0;
		for (Kind<?> k : kinds) {
			ks[i++] = k;
		}
		p.register(ws, ks);
		ConsoleUtil.debug(getClass(), String.format("re-register %s %s", p, Arrays.toString(ks)));
	}

	private Set<Kind<?>> getKinds(Set<WatchItem> items) {
		Set<Kind<?>> kinds = new HashSet<Kind<?>>();
		for (WatchItem wi : items) {
			kinds.addAll(Arrays.asList(wi.kinds));
		}
		return kinds;
	}

	/**
	 * 
	 * @param item
	 *            this watch item doesn't have to be monitored before, for
	 *            example, a child file who's parent have been monitored can be
	 *            unmonitored alone.
	 * @throws IOException
	 */
	public void unmonitor(WatchItem item) throws IOException {
		if (item.p != null) {
			Set<WatchItem> items = watchItems.get(item.p);
			if (items != null) {
				Set<Kind<?>> before = new HashSet<WatchEvent.Kind<?>>();
				before.addAll(getKinds(items));
				if (items.remove(item)) {
					ConsoleUtil.debug(getClass(), "un-monitor: " + item);
					Set<Kind<?>> after = getKinds(items);
					if (!after.containsAll(before)) {
						// kinds have been decreased
						reregister(item.p, after);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @throws IOException
	 *             see @{@link WatchService #close()}.
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
							if (item.f.getName().equals(e.context().toString())
									|| item.f.toPath().equals(key.watchable())) {
								for (Kind<?> k : item.kinds) {
									if (k == e.kind()
											&& (item.f.exists() || k.equals(StandardWatchEventKinds.ENTRY_DELETE))) {
										ConsoleUtil.debug(FileMonitor.this.getClass(),
												String.format("%n  path: %s%n  event: %s - %s%n  item: %s",
														key.watchable(), e.context(), e.kind().name(), item));
										item.onEvent(e);
										break;
									}
								}
							}
						}
					}
				}
				key.reset();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ClosedWatchServiceException e) {
				ConsoleUtil.debug(getClass(), "exit.");
			}
		}
	}

	public static abstract class WatchItem {
		private Kind<?>[] kinds;
		private File f;
		private Path p;

		/**
		 * 
		 * @param file file or directory to monitor.
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

		protected abstract void onEvent(WatchEvent<?> e);
	}

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

		try {
			FileMonitor fcm = new FileMonitor();
			fcm.monitor(new WatchItem(new File("e:/aa/"), StandardWatchEventKinds.ENTRY_MODIFY,
					StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.OVERFLOW) {

				@Override
				protected void onEvent(WatchEvent<?> e) {
				}
			});
			new Thread(fcm).start();
			Thread.sleep(6000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}

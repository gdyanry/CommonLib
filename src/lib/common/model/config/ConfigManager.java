/**
 * 
 */
package lib.common.model.config;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import lib.common.model.FileMonitor;
import lib.common.model.FileMonitor.WatchItem;
import lib.common.util.ConsoleUtil;
import lib.common.util.IOUtil;

/**
 * A configuration manager that supports auto-reload on change of configuration
 * file without restarting hosting application.
 * 
 * @author yanry
 * 
 *         2014年7月3日 下午10:55:54
 */
public abstract class ConfigManager {
	private LinkedHashMap<String, Object> defaults;
	private LinkedHashMap<String, String> loads;
	private File configFile;
	private String charset;
	private String entrySeperator;
	private String keyValueSeparator;
	private StringBuilder keyDescription;

	/**
	 * 
	 * @param configFile
	 *            configuration file.
	 * @param charset
	 *            character set used in configuration file.
	 * @param keyValueSeparator
	 *            separator string between configuration key and value.
	 * @param entrySeperator
	 *            separator between configuration items.
	 * @param monitor
	 *            pass null to disable monitoring configuration file.
	 * @param configClasses
	 *            the configuration items must be type of {@link ConfigItem}.
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public ConfigManager(File configFile, String charset, String keyValueSeparator, String entrySeperator,
			FileMonitor monitor, Class<?>... configClasses)
					throws UnsupportedEncodingException, IOException, IllegalArgumentException, IllegalAccessException {
		this.configFile = configFile;
		this.charset = charset;
		this.keyValueSeparator = keyValueSeparator;
		this.entrySeperator = entrySeperator;
		defaults = new LinkedHashMap<String, Object>();
		keyDescription = new StringBuilder();
		initialize(configClasses);
		ConsoleUtil.debug("init config: " + defaults);
		if (!configFile.exists()) {
			createConfigFile();
		}
		load();
		createReadMe(String.format("手动生成配置文件时，应使用%s字符集编码。", charset));
		if (monitor != null) {
			monitor.monitor(new WatchItem(configFile, StandardWatchEventKinds.ENTRY_MODIFY) {

				@Override
				protected void onEvent(WatchEvent<?> e) {
					try {
						beforeReload();
						load();
						afterReload();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * Create configuration file using default initialized key values. This will
	 * override the existing configuration file!
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public synchronized void createConfigFile() throws UnsupportedEncodingException, IOException {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Object> entry : defaults.entrySet()) {
			sb.append(entry.getKey()).append(keyValueSeparator).append(entry.getValue()).append(entrySeperator);
		}
		IOUtil.stringToFile(sb.toString(), configFile, charset, false);
		ConsoleUtil.debug("create config file: " + configFile);
	}

	/**
	 * Load configuration from file to memory.
	 * 
	 * @throws IOException
	 */
	public synchronized void load() throws IOException {
		if (configFile.exists()) {
			String str = IOUtil.fileToString(configFile, charset);
			String[] entries = str.split(entrySeperator);
			if (loads == null) {
				loads = new LinkedHashMap<String, String>(entries.length);
			}
			loads.clear();
			for (String entry : entries) {
				String[] keyValue = entry.split(keyValueSeparator);
				if (keyValue.length == 2) {
					loads.put(keyValue[0].trim(), keyValue[1].trim());
				}
			}
			ConsoleUtil.debug("load config: " + loads);
		}
	}

	/**
	 * Get value of the specific item as string.
	 * 
	 * @param item
	 * @return
	 */
	public String getString(ConfigItem item) {
		String v = loads.get(item.name);
		if (v == null) {
			Object obj = defaults.get(item.name);
			if (obj != null) {
				return obj.toString();
			}
		}
		return v;
	}

	/**
	 * Get value of the specific item as integer.
	 * 
	 * @param item
	 * @return
	 */
	public int getInt(ConfigItem item) {
		return Integer.parseInt(getString(item));
	}

	/**
	 * Generate description file of configuration items in the same folder with
	 * configuration file.
	 * 
	 * @param content
	 *            content appended to the beginning of the description file.
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public void createReadMe(String content) throws UnsupportedEncodingException, IOException {
		// append content
		StringBuilder sb = new StringBuilder();
		if (content != null) {
			sb.append(content).append(System.getProperty("line.separator"));
		}
		// append key description
		if (keyDescription.length() > 0) {
			sb.append("===========================配置项说明========================").append(System.getProperty("line.separator"))
					.append(keyDescription);
		}
		// save
		File manual = new File(configFile.getParentFile(), configFile.getName() + ".readme");
		IOUtil.stringToFile(sb.toString(), manual, charset, false);
		ConsoleUtil.debug("create config manual: " + manual);
	}

	private void initialize(Class<?>... cs) throws IllegalArgumentException, IllegalAccessException {
		// get name field of ConfigItem
		Field[] fields = ConfigItem.class.getFields();
		Field nameField = null;
		for (Field f : fields) {
			if (f.getAnnotation(ConfigItemName.class) != null) {
				nameField = f;
			}
		}
		// iterate configuration items
		for (Class<?> c : cs) {
			fields = c.getFields();
			for (Field f : fields) {
				if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(ConfigItem.class)) {
					// set name at runtime
					ConfigItem item = (ConfigItem) f.get(null);
					nameField.set(item, f.getName());
					String key = item.name;
					String description = item.getDescription();
					// put into default configurations
					defaults.put(key, item.getValue());
					if (description != null) {
						keyDescription.append(key).append(": ").append(description).append(System.getProperty("line.separator"));
					}
				}
			}
		}
	}

	/**
	 * Phase after configuration file has been changed and before in-memory
	 * configuration has been reloaded.
	 */
	protected abstract void beforeReload();

	/**
	 * Phase after in-memory configuration has been reloaded.
	 */
	protected abstract void afterReload();
}

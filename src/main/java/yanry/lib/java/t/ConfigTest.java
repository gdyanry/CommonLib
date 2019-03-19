package yanry.lib.java.t;

import yanry.lib.java.model.FileMonitor;
import yanry.lib.java.model.config.ConfigItem;
import yanry.lib.java.model.config.ConfigManager;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author yanry
 *
 *         2015年1月7日 下午3:57:52
 */
public class ConfigTest {
	public static final ConfigItem SERVER = new ConfigItem("172.168.1.1", "服务器地址哦");
	public static final ConfigItem PORT = new ConfigItem(8080, "端口号");
	public static final ConfigItem CACHE_SIZE = new ConfigItem(24, "缓存大小");
	
	public static void main(String[] args) {
		try {
			FileMonitor fm = new FileMonitor();
			new Thread(fm).start();
			ConfigManager cm = new ConfigManager(new File("f:/test.config"), "gbk", "=",
					System.lineSeparator(), fm, ConfigTest.class) {

						@Override
						protected void beforeReload() {
							// TODO Auto-generated method stub
							
						}

						@Override
						protected void afterReload() {
							// TODO Auto-generated method stub
							
						}};
			System.out.println(cm.getInt(CACHE_SIZE));
			Thread.sleep(60000);
			System.out.println(cm.getInt(CACHE_SIZE));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

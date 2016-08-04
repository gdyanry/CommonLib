/**
 * 
 */
package lib.common.model.config;


/**
 * Entity class of configuration item, whose key name is the name of instance variable.
 * @author yanry
 *
 *         2015年1月7日 下午3:01:07
 */
public class ConfigItem {
	private Object value;
	private String description;

	@ConfigItemName
	public String name;

	public ConfigItem(Object value, String description) {
		super();
		this.value = value;
		this.description = description;
	}

	public Object getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

}

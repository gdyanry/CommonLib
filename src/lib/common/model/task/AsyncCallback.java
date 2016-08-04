/**
 * 
 */
package lib.common.model.task;

/**
 * @author yanry
 *
 *         2016年5月29日
 */
public interface AsyncCallback<T> {
	void onResult(T resultData);
}

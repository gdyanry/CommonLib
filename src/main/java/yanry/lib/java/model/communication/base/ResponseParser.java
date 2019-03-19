package yanry.lib.java.model.communication.base;

/**
 * @author yanry
 *
 * 2016年6月29日
 */
public interface ResponseParser {

	/**
	 * 
	 * @param responseData
	 * @return return null if the response data is not business success.
	 */
	Object getBusinessSuccessData(Object responseData);
}

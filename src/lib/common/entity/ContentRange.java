/**
 * 
 */
package lib.common.entity;

/**
 * This is applicable when the http request has "RANGE" header, by using toString() as "Content-Range" value in response header.
 * 若同时指定几个范围，如：bytes=500-600,601-999，只取第一项。
 * @author yanry
 *
 * 2014年12月31日 上午11:15:19
 */
public class ContentRange {
	private int startPos;
	private int endPos;
	private String contentRange;
	
	/**
	 * 
	 * @param range value of the "RANGE" header.
	 * @param length total length of the requested resource.
	 */
	public ContentRange(String range, int length) {
		endPos = length - 1;
		if (range != null) {
			String[] arr = range.replaceAll("bytes=", "").split(",")[0].split("-");
			if (arr[0].length() > 0) {
				startPos = Integer.parseInt(arr[0]);
			}
			if (arr.length == 2) {
				endPos = Integer.parseInt(arr[1]);
			}
			// last xx bytes.
			if (arr[0].length() == 0 && arr.length == 2) {
				startPos = length - endPos;
				endPos = length - 1;
			}
			if (startPos < 0) {
				startPos = 0;
			}
			if (startPos > length) {
				startPos = length;
			}
			if (endPos >= length) {
				endPos = length - 1;
			}
		}
		contentRange = new StringBuilder("bytes ").append(startPos).append("-").append(endPos).append("/").append(length).toString();
	}
	
	public int getStartPosition() {
		return startPos;
	}
	
	/**
	 * 
	 * @return the actual length of response entity, typically used to set "Content-Length" header in response.
	 */
	public int getContentLength() {
		return endPos - startPos + 1;
	}
	
	@Override
	public String toString() {
		return contentRange;
	}
}

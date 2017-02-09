/**
 * 
 */
package lib.common.t;

import lib.common.model.json.JSONObject;
import lib.common.util.ConversionUtil;
import lib.common.util.HexUtil;
import lib.common.util.IOUtil;
import lib.common.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Random;



/**
 * @author yanry
 *
 *         2015年2月11日 上午9:34:13
 */
public class Test {
	
	public static void main(String[] args) {
		System.out.println(IOUtil.getAppRelativeFile("").getAbsolutePath());
		System.out.println();
		byte[] arr = {-122, 1,0,0};
		System.out.println(ConversionUtil.byteArrayToInt(arr, 0, null));
		System.out.println(Arrays.toString(ConversionUtil.intToByteArray(36868, null)));
		
		System.out.println(2 >> 1);
		System.out.println(Math.pow(2, 6));
		System.out.println(StringUtil.generateFixedLengthNumber(new Random(), 10));

		System.out.println(1<<2);
		
		JSONObject jo1 = new JSONObject().put("2", 2);
		JSONObject jo2 = new JSONObject().put("2", 2);
		System.out.println(jo1.equals(jo2));
		System.out.println(jo1.toString().equals(jo2.toString()));
		
		try {
			System.out.println(HexUtil.charsetHex("中文", "utf-8", 20));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(~1);
		System.out.println(~0);
		System.out.println(String.format("%tc", 1466776535000L));

        System.out.println(new JSONObject() instanceof Object);
    }
}

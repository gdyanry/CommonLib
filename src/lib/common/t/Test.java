/**
 * 
 */
package lib.common.t;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Random;

import lib.common.model.json.JSONObject;
import lib.common.util.ConversionUtil;
import lib.common.util.HexUtil;
import lib.common.util.IOUtil;
import lib.common.util.StringUtil;



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
		String s = "https://s2.meijiabang.cn/v1/user/groups.json?fields=list[].group_id,list[].icon,list[].ui_type,list[].can_create_topic,list[].topic_count,list[].name,list[].type,list[].desc,list[].best_topic_count,list[].popularity_users[].uid,list[].popularity_users[].nickname,list[].popularity_users[].verified_type,list[].popularity_users[].cpma.icon,list[].popularity_users[].avatar.m(320|webp)&type=custom";
		System.out.println(s.charAt(377));
	}
}

package yanry.lib.java.t;

import yanry.lib.java.interfaces.StreamTransferHook;
import yanry.lib.java.model.http.HttpMultipart;
import yanry.lib.java.model.json.JSONArray;
import yanry.lib.java.model.json.JSONObject;
import yanry.lib.java.util.StringUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

public class MessageBox {
    private static final String APPID = "XpAit2i87h9QEwNtkjVp";
    private static final String APPKEY = "VkkcE1Q2Y3By5zTn5PoW";
    private static final String SECRET = "FIWfrFg8W1jFJggq8Lxo";
    private static final String TEMPLATE_ID = "10017";
    private static final String URL_PRODUCT = "http://api.msg.tcloudfamily.com:58080/push-open/router";
    private static final String URL_TEST = "http://121.201.96.245:26178/push-open/router";
    private static final String CHARSET = "utf-8";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        push(true);
    }

    private static void push(boolean isTest) throws IOException, NoSuchAlgorithmException {
        HttpMultipart form = new HttpMultipart(isTest ? URL_TEST : URL_PRODUCT, null) {
            @Override
            protected StreamTransferHook getUploadHook() {
                return null;
            }

            @Override
            protected String getCharset() {
                return CHARSET;
            }
        };
        TreeMap<String, String> params = new TreeMap<>();
        params.put("method", "apppush");
        params.put("appKey", APPKEY);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("v", "1.0");
        params.put("appId", APPID);
        params.put("appPackage", "com.tcl.common.pushservice");
        params.put("openType", "service");
        params.put("open", "com.tcl.common.pushservice.PushReceiverService");
        params.put("channel", "1");
        params.put("openappforbd", "0");
        params.put("msgType", "text");
        params.put("appDatas", new JSONObject().put("testkey", "aaa").toString());
        params.put("templateId", TEMPLATE_ID);
        params.put("pushTimeRange", "1");
        params.put("pushType", "1");
        params.put("conditionType", "condition");
        params.put("conditionLogic", "and");
        params.put("conditions", new JSONArray().put(new JSONObject().put("type", "dnum").put("value", "527512505").put("operator", "in")).toString());
        // sign
        StringBuilder sb = new StringBuilder(SECRET);
        for (String key : params.keySet()) {
            sb.append(key).append(params.get(key));
        }
        sb.append(SECRET);
        params.put("sign", StringUtil.digest(sb.toString(), CHARSET, "MD5"));
        System.out.println(params);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            form.addText(entry.getKey(), URLEncoder.encode(entry.getValue(), CHARSET));
        }
        form.commit();
        System.out.println(form.getString(CHARSET));
    }

    private static void addParam(HttpMultipart form, String param, Object value) throws IOException {
        form.addText(param, URLEncoder.encode(value.toString(), CHARSET));
    }
}

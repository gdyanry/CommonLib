package yanry.lib.java.t;

import yanry.lib.java.interfaces.StreamTransferHook;
import yanry.lib.java.model.http.HttpMultipart;

import java.io.IOException;
import java.net.URLEncoder;

public class MessageBox {
    private static final String APPID = "XpAit2i87h9QEwNtkjVp";
    private static final String APPKEY = "VkkcE1Q2Y3By5zTn5PoW";
    private static final String SECRET = "FIWfrFg8W1jFJggq8Lxo";
    private static final String URL_PRODUCT = "http://api.msg.tcloudfamily.com:58080/push-open/router";
    private static final String URL_TEST = "http:// 121.201.96.245:26178/push-open/router";
    private static final String CHARSET = "utf-8";

    public static void main(String[] args) {

    }

    private static void push(boolean isTest) throws IOException {
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
        addParam(form, "method", "apppush");
        addParam(form, "appKey", APPKEY);
        addParam(form, "timestamp", System.currentTimeMillis());
//        addParam(form, "sign", );

    }

    private static void addParam(HttpMultipart form, String param, Object value) throws IOException {
        form.addText(param, URLEncoder.encode(value.toString(), CHARSET));
    }
}

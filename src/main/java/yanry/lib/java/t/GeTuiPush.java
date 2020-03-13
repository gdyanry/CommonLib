package yanry.lib.java.t;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import yanry.lib.java.interfaces.StreamTransferHook;
import yanry.lib.java.model.Singletons;
import yanry.lib.java.model.http.HttpGet;
import yanry.lib.java.model.http.HttpPost;
import yanry.lib.java.model.http.Https;
import yanry.lib.java.model.json.JSONObject;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.model.log.extend.ConsoleHandler;
import yanry.lib.java.model.log.extend.SimpleFormatter;
import yanry.lib.java.util.AesUtil;
import yanry.lib.java.util.IOUtil;
import yanry.lib.java.util.StringUtil;

public class GeTuiPush {
    private static final byte[] IV = new byte[]{65, 20, -91, 123, -102, 126, 105, -28, -15, 13, 51, 32, 53, 45, -97, -40};
    private static final byte[] KEY = new byte[]{-49, 59, -97, -82, 5, -125, -92, -15, -7, -4, 95, -87, 85, -47, -34, -10};

    public static void main(String... args) throws IOException, GeneralSecurityException {
        SimpleFormatter formatter = new SimpleFormatter();
        formatter.addFlag(SimpleFormatter.SEQUENCE_NUMBER).addFlag(SimpleFormatter.METHOD);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        Logger.getDefault().addHandler(handler);
        Map<String, InputStream> certificates = new HashMap<>();
        certificates.put("tclking", new FileInputStream("f:/tclking_cert/tclking.crt"));
        Https.initSSL(null, certificates, null, null);
        pushWeatherAlarm(false, false);
//        pushAiUpdate();
//        getTaskId(10324);
        System.out.println(getAlias("115051789"));
    }

    private static void pushWeatherAlarm(boolean isDebug, boolean byRegion) throws IOException {
        String dataToPush = "{\"signName\":\"腾讯天气\",\"templateCode\":\"10025\",\"param\":{\"locale\":\"深圳市南山区\",\"date\":\"%s\",\"type\":\"台风\",\"level\":\"橙色\"},\"region\":\"%s\",\"did\":\"%s\",\"custom\":null}";
        String region = byRegion ? "440305" : "";
        String deviceId = byRegion ? "" : "9VQ84HLJd6bWPDTzsbgPMQ二二";
        dataToPush = String.format(dataToPush, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()), region, deviceId);
        String appId = "x0oTtOgtzOPM1dRui3pLIsts4nC0CUjX";
        String secret = "qGai6LBHLE6idnwhuAMpoKC6UDavmboh";
        String requestPrefix = isDebug ? "http://bigdata" : "https://open";
        HttpGet get = new HttpGet(String.format("%s.tclking.com/push/token?appid=%s&secret=%s&ver=1.0", requestPrefix, appId, secret));
        if (!get.isSuccess()) {
            System.err.println(IOUtil.streamToString(get.getConnection().getErrorStream(), "utf-8"));
            return;
        }
        String resp = get.getString("utf-8");
        System.out.println(resp);
        JSONObject respJson = new JSONObject(resp);
        String token = respJson.getJSONObject("data").getString("accessToken");
        HashMap<String, Object> urlParams = new HashMap<>();
        urlParams.put("accessToken", token);
        urlParams.put("ver", 1.0f);
        HttpPost post = new HttpPost(requestPrefix + ".tclking.com/push/3rd", urlParams, "application/json") {
            @Override
            protected StreamTransferHook getUploadHook() {
                return null;
            }
        };
        post.send(dataToPush.getBytes("utf-8"));
        if (post.isSuccess()) {
            System.out.println(post.getString("utf-8"));
        } else {
            System.err.println(IOUtil.streamToString(post.getConnection().getErrorStream(), "utf-8"));
        }
    }

    private static void pushAiUpdate() throws IOException {
        String requestPrefix = "https://open";
        String appId = "FTjo3FqoJZNAVxNySkimMLygvqMBh9MK";
        String secret = "cQ65m2fKN2LFUuRHovD6axWME0RBz5jL";
        HttpGet get = new HttpGet(String.format("%s.tclking.com/push/token?appid=%s&secret=%s&ver=1.0", requestPrefix, appId, secret));
        String resp = get.getString("utf-8");
        System.out.println(resp);
        JSONObject respJson = new JSONObject(resp);
        String token = respJson.getJSONObject("data").getString("accessToken");
        String dataToPush = "{\"signName\":\"瓦力\",\"param\":{\"type\":\"%tT\"},\"did\":\"LIrsAMgFGoUzPk5veKVSIQ二二\",\"tag\":\"\"}";
        dataToPush = String.format(dataToPush, System.currentTimeMillis());
        HashMap<String, Object> params = new HashMap<>();
        params.put("accessToken", token);
        params.put("ver", 1.0f);
        HttpPost post = new HttpPost(requestPrefix + ".tclking.com/push/3rd", params, "application/json") {
            @Override
            protected StreamTransferHook getUploadHook() {
                return null;
            }
        };
        post.send(dataToPush.getBytes("utf-8"));
        if (post.isSuccess()) {
            System.out.println(post.getString("utf-8"));
        } else {
            System.err.println(post.getConnection().getResponseCode());
        }
    }

    private static void addTag(String tag) throws IOException, NoSuchAlgorithmException {
        JSONObject params = new JSONObject();
        String clientId = "4018c4ea57043a38e32323f2001c5264";
        params.put("cid", clientId);
        params.put("tag", tag);
        long timestamp = System.currentTimeMillis();
        params.put("timestamp", timestamp);
        int nonce = Singletons.get(Random.class).nextInt();
        params.put("nonce", nonce);
        HttpPost post = new HttpPost("http://bigdata.tclking.com/push/tag", null, "application/json") {
            @Override
            protected StreamTransferHook getUploadHook() {
                return null;
            }
        };
        String charset = "utf-8";
        String input = URLEncoder.encode(clientId + tag + timestamp + nonce + "tcl_cloud_push", charset);
        post.getConnection().setRequestProperty("sign", StringUtil.digest(input, charset, "MD5"));
        post.send(params.toString().getBytes(charset));
        post.getConnection().connect();
        if (post.isSuccess()) {
            String resp = post.getString(charset);
            Logger.getDefault().vv(resp);
        } else {
            int responseCode = post.getConnection().getResponseCode();
            Logger.getDefault().ee(responseCode);
        }
    }

    private static void getTaskId(int queryId) throws IOException {
        HttpGet get = new HttpGet("https://open.tclking.com/push/pushlog?id=" + queryId);
        get.send();
        if (get.isSuccess()) {
            String resp = get.getString("utf-8");
            System.out.println(resp);
        } else {
            System.err.println(get.getConnection().getResponseCode());
        }
    }

    private static String getAlias(String deviceNum) throws UnsupportedEncodingException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        String charsetName = "utf-8";
        byte[] encrypt = AesUtil.encrypt(IV, KEY, deviceNum.getBytes(charsetName));
        String base64 = Base64.getEncoder().encodeToString(encrypt);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < base64.length(); i++) {
            char c = base64.charAt(i);
            switch (c) {
                case '=':
                    sb.append('二');
                    break;
                case '+':
                    sb.append('十');
                    break;
                case '/':
                    sb.append('丿');
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
}

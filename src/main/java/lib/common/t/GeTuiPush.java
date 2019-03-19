package lib.common.t;

import lib.common.interfaces.StreamTransferHook;
import lib.common.model.Singletons;
import lib.common.model.http.HttpGet;
import lib.common.model.http.HttpPost;
import lib.common.model.http.Https;
import lib.common.model.json.JSONObject;
import lib.common.model.log.Logger;
import lib.common.util.StringUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GeTuiPush {
    public static void main(String... args) throws IOException, GeneralSecurityException {
        Map<String, InputStream> certificates = new HashMap<>();
        certificates.put("tclking", new FileInputStream("f:/tclking_cert/tclking.crt"));
        Https.initSSL(null, certificates, null, null);
        pushWeatherAlarm(false, false);
//        pushAiUpdate();
//        getTaskId(10324);
    }

    private static void pushWeatherAlarm(boolean isDebug, boolean byRegion) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String requestPrefix = isDebug ? "http://bigdata" : "https://open";
        String appId = isDebug ? "8cf1ffc22a04982c8e435223dd951fb7" : "x0oTtOgtzOPM1dRui3pLIsts4nC0CUjX";
        String secret = isDebug ? "35223dd9b797db9e2a04982c8e435223" : "qGai6LBHLE6idnwhuAMpoKC6UDavmboh";
        HttpGet get = new HttpGet(String.format("%s.tclking.com/push/token?appid=%s&secret=%s&ver=1.0", requestPrefix, appId, secret));
        String resp = get.getString("utf-8");
        System.out.println(resp);
        TencentQuery.query("天气");
        JSONObject respJson = new JSONObject(resp);
        String token = respJson.getJSONObject("data").getString("accessToken");
        String dataToPush = "{\"signName\":\"腾讯天气\",\"templateCode\":\"10025\",\"param\":{\"locale\":\"深圳市南山区\",\"date\":\"2018-03-05 18:06\",\"type\":\"台风\",\"level\":\"黄色\"},\"region\":\"%s\",\"did\":\"%s\",\"custom\":null}";
        String region = byRegion ? "440305" : "";
        String deviceId = byRegion ? "" : "LIrsAMgFGoUzPk5veKVSIQ二二";
        dataToPush = String.format(dataToPush, region, deviceId);
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
}

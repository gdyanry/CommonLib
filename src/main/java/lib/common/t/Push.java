package lib.common.t;

import lib.common.interfaces.StreamTransferHook;
import lib.common.model.Singletons;
import lib.common.model.http.HttpGet;
import lib.common.model.http.HttpPost;
import lib.common.model.json.JSONObject;
import lib.common.model.log.Logger;
import lib.common.util.StringUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;

public class Push {
    public static void main(String... args) throws IOException, NoSuchAlgorithmException {
        addTag("宝安区");
//        pushWeatherAlarm();
    }

    private static void pushWeatherAlarm() throws IOException {
        HttpGet get = new HttpGet("http://bigdata.tclking.com/push/token?appid=8cf1ffc22a04982c8e435223dd951fb7&secret=35223dd9b797db9e2a04982c8e435223&ver=1.0");
        String resp = get.getString("utf-8");
        System.out.println(resp);
        JSONObject respJson = new JSONObject(resp);
        String token = respJson.getJSONObject("data").getString("accessToken");
        String dataToPush = "{\"signName\":\"腾讯天气\",\"templateCode\":\"10025\",\"param\":{\"locale\":\"深圳市南山区\",\"date\":\"2018-03-05 18:06\",\"type\":\"暴雨\",\"level\":\"红色\"},\"region\":\"%s\",\"did\":\"%s\",\"custom\":null}";
        String region = "宝安区";
        String deviceId = "";
        dataToPush = String.format(dataToPush, region, deviceId);
        HashMap<String, Object> params = new HashMap<>();
        params.put("accessToken", token);
        params.put("ver", 1.0f);
        HttpPost post = new HttpPost("http://bigdata.tclking.com/push/3rd", params, "application/json") {
            @Override
            protected StreamTransferHook getUploadHook() {
                return null;
            }
        };
        post.send(dataToPush.getBytes("utf-8"));
        System.out.println(post.isSuccess());
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
        post.getConnection().setRequestProperty("sign", StringUtil.encrypt(input, charset, "MD5"));
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
}

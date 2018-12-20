package lib.common.t;

import lib.common.interfaces.StreamTransferHook;
import lib.common.model.http.HttpGet;
import lib.common.model.http.HttpPost;
import lib.common.model.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class Push {
    public static void main(String... args) throws IOException {
        HttpGet get = new HttpGet("http://bigdata.tclking.com/push/token?appid=8cf1ffc22a04982c8e435223dd951fb7&secret=35223dd9b797db9e2a04982c8e435223&ver=1.0");
        String resp = get.getString("utf-8");
        System.out.println(resp);
        JSONObject respJson = new JSONObject(resp);
        String token = respJson.getJSONObject("data").getString("accessToken");
        String dataToPush = "{\"signName\":\"腾讯天气\",\"templateCode\":\"10025\",\"param\":{\"locale\":\"深圳市南山区\",\"date\":\"2018-03-05\",\"type\":\"暴雨\",\"level\":\"红色\"},\"region\":\"\",\"did\":\"%s\",\"custom\":null}";
        String deviceId = "4ea04644d75511875a56edfb760825d2d66914fd";
        dataToPush = String.format(dataToPush, deviceId);
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
}

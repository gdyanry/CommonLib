package yanry.lib.java.t;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;

import yanry.lib.java.interfaces.StreamTransferHook;
import yanry.lib.java.model.http.HttpPost;
import yanry.lib.java.model.json.JSONObject;
import yanry.lib.java.model.json.pattern.JsonObjectPattern;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.util.IOUtil;
import yanry.lib.java.util.StringUtil;
import yanry.lib.java.util.object.ObjectUtil;

public class BaiduQuery {
    public static void main(String... args) throws IOException {
        parseBaikeNluPatterns();
//        parseBaikePatterns();
//        userInteract();
    }

    private static void parseBaikeNluPatterns() throws IOException {
        HashMap<String, JsonObjectPattern> patterns = new HashMap<>();
        parseBaike("nlu", jsonObject -> {
            JSONObject result = jsonObject.getJSONObject("result");
            JSONObject nlu = result.getJSONObject("nlu");
            String key = new StringBuilder()
                    .append(result.optString("bot_id")).append('|')
                    .append(nlu.getString("domain")).append('|')
                    .append(nlu.getString("intent")).append('|')
                    .append(nlu.optString("sub_intent")).toString();
            JsonObjectPattern pattern = patterns.get(key);
            if (pattern == null) {
                patterns.put(key, JsonObjectPattern.get(jsonObject, 100));
            } else {
                patterns.put(key, pattern.and(jsonObject));
            }
            return key;
        });
        IOUtil.stringToFile(StringUtil.formatJson(new JSONObject(patterns).toString()), "e:/baike/nlu_patterns_ex.txt", "utf-8", false);
    }

    private static void parseBaike(String tag, Function<JSONObject, Object> keySupplier) throws IOException {
        HashMap<Object, Set<String>> map = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("e:/baike/guide_words.txt"), "utf-8"));
        String query;
        while ((query = reader.readLine()) != null) {
            String respStr = query(query);
            JSONObject respJson = new JSONObject(respStr);
            Object key = keySupplier.apply(respJson);
            Set<String> querySet = map.get(key);
            if (querySet == null) {
                querySet = new HashSet<>();
                map.put(key, querySet);
            }
            querySet.add(query);
        }
        String presentation = ObjectUtil.getPresentation(map).toString();
        IOUtil.stringToFile(presentation, String.format("e:/baike/%s_patterns.txt", tag), "utf-8", false);
        IOUtil.stringToFile(StringUtil.formatJson(presentation), String.format("e:/baike/%s_patterns_formatted.txt", tag), "utf-8", false);
    }

    private static void parseBaikePatterns() throws IOException {
        JsonObjectPattern[] common = new JsonObjectPattern[1];
        parseBaike("full", jsonObject -> {
            JsonObjectPattern pattern = JsonObjectPattern.get(jsonObject, 100);
            if (common[0] == null) {
                common[0] = pattern;
            } else {
                common[0] = common[0].and(jsonObject);
            }
            return pattern;
        });
        String raw = common[0].toJSONString();
        Logger.getDefault().ii(raw);
        IOUtil.stringToFile(StringUtil.formatJson(raw), "e:/baike/common_pattern.txt", "utf-8", false);
    }

    private static void userInteract() throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println();
            System.out.println("query:");
            String readLine = scanner.nextLine();
            if (readLine.equalsIgnoreCase("exit")) {
                return;
            } else if (readLine.length() > 0) {
                System.err.println("###");
                query(readLine);
            }
        }
    }

    private static String query(String query) throws IOException {
        HttpPost post = new HttpPost("https://xiaodu.baidu.com:443/saiya/ws", null, "application/json") {
            @Override
            protected StreamTransferHook getUploadHook() {
                return null;
            }
        };
        String ver2 = "{\"appid\":\"dm7F5A288A12AC3F6C\",\"appkey\":\"B6943A24DA60A7B77D58ED53E95A5206\",\"sdk_ui\":\"no\",\"sdk_init\":\"no\",\"appname\":\"com.tcl.walleve\",\"channel_from\":\"\",\"channel_ctag\":\"\",\"from_client\":\"sdk\",\"hint_id\":\"\",\"client_msg_id\":\"2132005e-807c-44e4-b98f-72aa4c7d89ac\",\"CUID\":\"1fbd9f106ff68b1cfc8f2561fd7e7d55\",\"OLD_CUID\":\"2e826fd90a8a752c62b57d3d6a14e8fd\",\"StandbyDeviceId\":\"F1C09004330BC1CAB555EDC1B94EE2F07CD2D18D0F414CD78A9FFC03331DD265\",\"operation_system\":\"android\",\"sample_name\":\"bear_brain_wireless\",\"request_uid\":\"1fbd9f106ff68b1cfc8f2561fd7e7d55\",\"app_ver\":\"2.0.4.4\",\"device_brand\":\"TCL\",\"device_model\":\"TCL Android TV\",\"device_event\":{},\"device_status\":{\"ai.dueros.device_interface.extensions.local_screen\":{\"source\":{\"type\":\"LAUNCHER\"},\"status\":\"RUNNING\"},\"UiControl\":{\"items\":[]}},\"device_interface\":{\"Capture\":{},\"System\":{},\"AudioPlayer\":{},\"UiControl\":{},\"ai.dueros.device_interface.tv_live\":{},\"VideoPlayer\":{},\"ai.dueros.device_interface.extensions.local_screen\":{},\"ai.dueros.device_interface.extensions.application\":{},\"CloudVideoPlayer\":{},\"ai.dueros.device_interface.extensions.smarthome\":{},\"ThirdpartyVideoCall\":{}},\"debug\":\"0\",\"request_query\":\"%s\",\"query_type\":\"0\",\"ais_switch\":0,\"network_status\":\"net_unknown\",\"BDUSS\":\"\",\"location_system\":\"\",\"longitude\":0,\"latitude\":0,\"_idx\":0,\"_status\":\"sending\",\"type\":\"user\",\"ctime\":1542867430,\"result_list\":[{\"result_type\":\"txt\",\"result_content\":{\"answer\":\"%<s\"}}],\"sort_key\":1542867430171}";
        String ver4 = "{\"appid\":\"dm0CCB008CB27184A1\",\"appkey\":\"0E22DFEE5CFA11C496D77854C62BEE07\",\"sdk_ui\":\"no\",\"sdk_init\":\"no\",\"appname\":\"com.tcl.walleve\",\"channel_from\":\"\",\"channel_ctag\":\"\",\"from_client\":\"sdk\",\"hint_id\":\"\",\"client_msg_id\":\"11b12e2f-2c85-45b7-9ed6-d9b776bab0fd\",\"CUID\":\"6dea8c62d6c587f80b7e67fd42cbfda2\",\"OLD_CUID\":\"6dea8c62d6c587f80b7e67fd42cbfda2\",\"StandbyDeviceId\":\"668DE64012F8E9FA9ADEEACD004A520A314A861AB9074A5E9D9090E7DBBAC6F4\",\"operation_system\":\"android\",\"sample_name\":\"bear_brain_wireless\",\"request_uid\":\"6dea8c62d6c587f80b7e67fd42cbfda2\",\"app_ver\":\"4.0.4.4\",\"device_brand\":\"MStar\",\"device_model\":\"X5\",\"device_event\":{},\"device_status\":{\"ai.dueros.device_interface.extensions.application\":{\"source\":{\"name\":\"com.tcl.cyberui\",\"packageName\":\"com.tcl.cyberui\"},\"status\":\"RUNNING\"}},\"device_interface\":{\"Capture\":{},\"System\":{},\"AudioPlayer\":{},\"UiControl\":{},\"ai.dueros.device_interface.tv_live\":{},\"VideoPlayer\":{},\"ai.dueros.device_interface.extensions.local_screen\":{},\"ai.dueros.device_interface.extensions.application\":{},\"CloudVideoPlayer\":{},\"ai.dueros.device_interface.extensions.smarthome\":{},\"ThirdpartyVideoCall\":{}},\"debug\":\"0\",\"request_query\":\"%s\",\"query_type\":\"0\",\"ais_switch\":0,\"network_status\":\"1_0\",\"BDUSS\":\"\",\"location_system\":\"\",\"longitude\":0,\"latitude\":0,\"_idx\":0,\"_status\":\"sending\",\"type\":\"user\",\"ctime\":1539333297,\"result_list\":[{\"result_type\":\"txt\",\"result_content\":{\"answer\":\"%<s\"}}],\"sort_key\":1539333297409}";
        String param = ver2;
        param = String.format(param, query);
        post.send(param.getBytes("utf-8"));
        String raw = null;
        if (post.isSuccess()) {
            raw = post.getString("utf-8");
            System.out.println(raw);
            System.out.println();
            System.out.println(String.format("time: %sms", post.getElapsedTimeMillis()));
            System.out.println(StringUtil.formatJson(raw));
        } else {
            HttpURLConnection connection = post.getConnection();
            System.err.println(String.format("%s - %s", connection.getResponseCode(), IOUtil.streamToString(connection.getErrorStream(), "utf-8")));
        }
        System.out.println();
        System.out.println(String.format("time: %sms", post.getElapsedTimeMillis()));
        return raw;
    }
}

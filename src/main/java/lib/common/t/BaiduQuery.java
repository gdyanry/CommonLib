package lib.common.t;

import lib.common.interfaces.StreamTransferHook;
import lib.common.model.http.HttpPost;
import lib.common.util.IOUtil;
import lib.common.util.StringUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Scanner;

public class BaiduQuery {
    public static void main(String... args) throws IOException {
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

    private static void query(String query) throws IOException {
        HttpPost post = new HttpPost("https://xiaodu.baidu.com:443/saiya/ws", null, "application/json") {
            @Override
            protected StreamTransferHook getUploadHook() {
                return null;
            }
        };
        String param = "{\"appid\":\"dm7F5A288A12AC3F6C\",\"appkey\":\"B6943A24DA60A7B77D58ED53E95A5206\",\"sdk_ui\":\"no\",\"sdk_init\":\"no\",\"appname\":\"com.tcl.walleve\",\"channel_from\":\"\",\"channel_ctag\":\"\",\"from_client\":\"sdk\",\"hint_id\":\"\",\"client_msg_id\":\"16bb6773-ace5-42f6-a748-2171c36e8cda\",\"CUID\":\"5c609efa1e5d904c3ceade6543927316\",\"OLD_CUID\":\"3925d3e935f032275e3e05a5ba18f51e\",\"StandbyDeviceId\":\"20E38E85983C13173488D524FA77579BC5EF75B93E1C4D018AE259BD74DD5686\",\"operation_system\":\"android\",\"sample_name\":\"bear_brain_wireless\",\"request_uid\":\"5c609efa1e5d904c3ceade6543927316\",\"app_ver\":\"2.0.4.4\",\"device_brand\":\"TCL\",\"device_model\":\"TCL Android TV\",\"device_event\":{},\"device_status\":{\"ai.dueros.device_interface.extensions.local_screen\":{\"source\":{\"type\":\"LAUNCHER\"},\"status\":\"RUNNING\"},\"UiControl\":{\"items\":[]}},\"device_interface\":{\"Capture\":{},\"System\":{},\"AudioPlayer\":{},\"UiControl\":{},\"ai.dueros.device_interface.tv_live\":{},\"VideoPlayer\":{},\"ai.dueros.device_interface.extensions.local_screen\":{},\"ai.dueros.device_interface.extensions.application\":{},\"CloudVideoPlayer\":{},\"ai.dueros.device_interface.extensions.smarthome\":{},\"ThirdpartyVideoCall\":{}},\"debug\":\"0\",\"request_query\":\"%s\",\"query_type\":\"0\",\"ais_switch\":0,\"network_status\":\"1_0\",\"BDUSS\":\"\",\"location_system\":\"\",\"longitude\":0,\"latitude\":0,\"_idx\":0,\"_status\":\"sending\",\"type\":\"user\",\"ctime\":1540890475,\"result_list\":[{\"result_type\":\"txt\",\"result_content\":{\"answer\":\"%<s\"}}],\"sort_key\":1540890475058}";
        param = String.format(param, query);
        post.send(param.getBytes("utf-8"));
        if (post.isSuccess()) {
            String raw = post.getString("utf-8");
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
    }
}

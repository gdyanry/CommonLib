package lib.common.t;

import lib.common.interfaces.StreamTransferHook;
import lib.common.model.http.HttpPost;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AiUi {
    public static void main(String... args) throws IOException {
        boolean isTestEnv = true;
        String server = isTestEnv ? "210.75.9.11" : "bigdata.tclking.com";
        String url = String.format("http://%s/speechcraft/info/talkingskill", server);
        Map<String, Object> params = new HashMap<>();
        params.put("version", 0);
        params.put("deviceType", "X8");
        HttpPost request = new HttpPost(url, params, "application/json") {
            @Override
            protected StreamTransferHook getUploadHook() {
                return null;
            }
        };
        request.send(new byte[0]);
        if (request.isSuccess()) {
            String response = request.getString("utf-8");
            System.out.println(response);
        } else {
            System.err.println(request.getConnection().getResponseCode());
        }
    }
}

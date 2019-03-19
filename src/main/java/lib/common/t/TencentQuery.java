package lib.common.t;

import lib.common.interfaces.StreamTransferHook;
import lib.common.model.http.HttpPost;
import lib.common.model.json.JSONObject;
import lib.common.util.StringUtil;
import lib.common.util.console.query.ConsoleQuery;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TencentQuery {
    public static void main(String... args) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        userInteract();
    }

    private static void userInteract() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        ConsoleQuery<String> query = new ConsoleQuery<String>() {
            @Override
            protected void appendPromptInfo(StringBuilder promptBuilder) {
            }

            @Override
            protected boolean isValid(String input) {
                return input.length() > 0;
            }

            @Override
            protected String map(String input) {
                return input;
            }
        };
        while (true) {
            String text = query.getValue("query");
            query(text);
        }
    }

    public static void query(String text) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        String botKey = "af86caf0c25711e89e580b8507394334";
        String botSecret = "b4e25a72b69a4bc193fecefec2e66f91";

        JSONObject postJson = new JSONObject().put("payload", new JSONObject().put("query", text));
        JSONObject header = new JSONObject().put("guid", "guid")
                .put("qua", "QV=3&PL=ADR&PR=chvoice&VE=7.6&VN=3350&PP=com.tencent.mtt&DE=TV")
                .put("user", new JSONObject().put("user_id", ""))
                .put("ip", "")
                .put("lbs", new JSONObject().put("latitude", 30.5434).put("longitude", 104.068));
        postJson.put("header", header);

        HttpPost post = new HttpPost("https://aiwx.html5.qq.com/api/v1/richanswer", null, "application/json; charset=UTF-8") {
            @Override
            protected StreamTransferHook getUploadHook() {
                return null;
            }
        };
        String data = postJson.toString();
        String dateTime = String.format("%tY%<tm%<tdT%<tH%<tM%<tSZ", System.currentTimeMillis());
        String signature = StringUtil.hmac((data + dateTime).getBytes("utf-8"), botSecret.getBytes("utf-8"), "hmacSHA256");
        String authorization = String.format("TVS-HMAC-SHA256-BASIC CredentialKey=%s, Datetime=%s, Signature=%s", botKey, dateTime, signature);
        post.getConnection().setRequestProperty("Authorization", authorization);
        post.send(data.getBytes("utf-8"));
        if (post.isSuccess()) {
            String resp = post.getString("utf-8");
            System.out.println(resp);
        } else {
            System.err.println(post.getConnection().getResponseCode());
        }
    }
}

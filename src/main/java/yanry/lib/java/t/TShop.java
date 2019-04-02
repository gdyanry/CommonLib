package yanry.lib.java.t;

import yanry.lib.java.model.http.HttpGet;
import yanry.lib.java.util.StringUtil;

import java.io.IOException;
import java.net.URLEncoder;

public class TShop {
    public static void main(String... args) throws IOException {
        System.out.println(StringUtil.formatJson(get("电视", 5, 1)));
    }

    public static String get(String goodsName, int size, int page) throws IOException {
        HttpGet get = new HttpGet(String.format("http://tshoptest.api.my7v.com/tvShop-search/ai/search?goodsName=%s&pageSize=%s&pageNum=%s",
                URLEncoder.encode(goodsName, "utf-8"), size, page));
        get.send();
        return get.getString("utf-8");
    }
}

package yanry.lib.java.t;

import yanry.lib.java.interfaces.StreamTransferHook;
import yanry.lib.java.model.Singletons;
import yanry.lib.java.model.http.HttpGet;
import yanry.lib.java.model.http.HttpPost;
import yanry.lib.java.model.json.JSONArray;
import yanry.lib.java.model.json.JSONException;
import yanry.lib.java.model.json.JSONObject;
import yanry.lib.java.model.log.Logger;
import yanry.lib.java.util.IOUtil;

import java.io.IOException;
import java.util.*;

public class AiUi {
    private static final boolean IS_TEST_ENV = false;
    private static final boolean IS_SUPPORT_FAR_FIELD_VOICE = false;
    private static final boolean IS_SUPPORT_FULL_SCENE = true;
    private static final int OUT_RESULT_SIZE = 500;

    private static final String GROUP_NAME_WORK_DAY = "工作日";
    private static final String GROUP_NAME_REST_DAY = "休假日";
    private static final String FIELD_NAME_CURRENT_TAB = "$当前Tab";
    private static final String FIELD_TAG_FULL_SCENE = "fullscene";
    private static final String FIELD_TAG_FESTIVAL = "festival";

    public static void main(String... args) throws IOException {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 16);
//        calendar.set(Calendar.HOUR_OF_DAY, 19);
//        calendar.set(Calendar.MINUTE, 33);
//        calendar.set(Calendar.SECOND, 24);
        System.out.println(requestData());
//        save(generate(new JSONObject(IOUtil.fileToString("g:/ai_data.txt", "gbk")), calendar.getTimeInMillis()), "g:/out.json");
//        save(generate(new JSONObject(requestData()).getJSONObject("data"), System.currentTimeMillis()), "g:/out.json");
        System.out.println(checkFestival(System.currentTimeMillis()));
    }

    private static String requestData() throws IOException {
        String server = IS_TEST_ENV ? "210.75.9.11" : "bigdata.tclking.com";
        String url = String.format("http://%s/speechcraft/info/talkingskill", server);
        Map<String, Object> params = new HashMap<>();
        params.put("version", 1579528642000L);
        params.put("deviceType", "TCL-CN-MS838A-X8");
        HttpPost request = new HttpPost(url, params, "application/json") {
            @Override
            protected StreamTransferHook getUploadHook() {
                return null;
            }
        };
        request.send(new byte[0]);
        if (request.isSuccess()) {
            String response = request.getString("utf-8");
            save(response, "g:/ai_guide_resp.txt");
            return response;
        } else {
            System.err.println(request.getConnection().getResponseCode());
            return null;
        }
    }

    private static void save(Object content, String filePath) throws IOException {
        IOUtil.stringToFile(content.toString(), filePath, "utf-8", false);
    }

    private static String getBaseUrl() {
        return IS_TEST_ENV ? "http://210.75.9.11/" : "http://bigdata.tclking.com/";
    }

    private static void print(Object msg) {
        System.out.println(msg);
    }

    private static JSONArray generate(JSONObject data, long now) throws JSONException {
        JSONArray groupList = data.getJSONArray("groupList");
        // 查询当天是否节假日
        Boolean isHoliday = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("date", String.format("%tY%<tm%<td", now));
            HttpGet get = new HttpGet(getBaseUrl() + "speechcraft/public/festivalcheck", params, 0);
            get.send();
            if (get.isSuccess()) {
                String resp = get.getString("utf-8");
                JSONObject respJson = new JSONObject(resp);
                int code = respJson.optInt("code");
                if (code == 200) {
                    isHoliday = respJson.optInt("data") != 0;
                } else {
                    Logger.getDefault().ee("判断是否节假日接口返回错误：", resp);
                }
            }
        } catch (IOException | JSONException e) {
            Logger.getDefault().catches(e);
        }
        if (isHoliday == null) {
            int dayOfWeek = GregorianCalendar.getInstance().get(Calendar.DAY_OF_WEEK);
            isHoliday = dayOfWeek == Calendar.SATURDAY | dayOfWeek == Calendar.SUNDAY;
        }
        // 先确定要选取的分组及其时间段
        JSONObject selectedSegment = null;
        String showTimeName = null;
        long[] showPeriod = null;
        long finishTime = Long.MAX_VALUE;
        long maxEffectiveTime = 0;
        for (int i = 0; i < groupList.length(); i++) {
            // 分组
            JSONObject group = groupList.getJSONObject(i);
            // 按生效/过期时间筛选
            long effectiveTime = group.optLong("effectiveTime");
            long expireTime = group.optLong("expireTime");
            if ((effectiveTime > 0 && effectiveTime > now || expireTime > 0 && expireTime < now)) {
                continue;
            }
            // 优先选取后面生效的组
            if (maxEffectiveTime > effectiveTime) {
                continue;
            }
            String groupName = group.getString("name");
            // 按休息/工作日筛选
            if (isHoliday && groupName.equals(GROUP_NAME_WORK_DAY) || !isHoliday && groupName.equals(GROUP_NAME_REST_DAY)) {
                continue;
            }
            // 处理分段
            JSONArray clientTypeList = group.getJSONArray("clientTypeList");
            // TODO 设备型号处理
            if (clientTypeList.length() > 0) {
                JSONArray segments = clientTypeList.getJSONObject(0).getJSONArray("useTimeList");
                _segment:
                for (int j = 0; j < segments.length(); j++) {
                    // 分段，每段有开始结束时间和若干个坑
                    JSONObject segment = segments.getJSONObject(j);
                    String useTime = segment.optString("useTime");
                    if (!isEmpty(useTime)) {
                        String[] showTime = useTime.split(",");
                        for (String timeName : showTime) {
                            long[] time = getTime(timeName, now);
                            if (now > time[0] && now < time[1]) {
                                selectedSegment = segment;
                                Logger.getDefault().v("find group: %s, segment: %s", groupName, timeName);
                                maxEffectiveTime = effectiveTime;
                                finishTime = expireTime > 0 ? Math.min(time[1], expireTime) : time[1];
                                showTimeName = timeName;
                                showPeriod = time;
                                break _segment;
                            }
                        }
                    } else {
                        selectedSegment = segment;
                        Logger.getDefault().v("find group: %s", groupName);
                        maxEffectiveTime = effectiveTime;
                        long[] time = getTime(null, now);
                        finishTime = expireTime > 0 ? Math.min(expireTime, time[1]) : time[1];
                        showTimeName = null;
                        showPeriod = time;
                        break;
                    }
                }
            }
        }
        JSONArray outResult = new JSONArray();
        if (selectedSegment == null) {
            Logger.getDefault().ww("no available group found.");
        } else {
            // 处理tab
            JSONArray tabList = data.getJSONArray("tabList");
            print("tabs: " + tabList);
            HashMap<String, StringBuilder> fieldTabBuilder = new HashMap<>();
            for (int i = 0; i < tabList.length(); i++) {
                JSONObject tab = tabList.getJSONObject(i);
                String tabName = tab.getString("name");
                String fieldName = tab.optString("fieldName");
                if (!isEmpty(fieldName)) {
                    StringBuilder tabOfField = fieldTabBuilder.get(fieldName);
                    if (tabOfField == null) {
                        tabOfField = new StringBuilder();
                        fieldTabBuilder.put(fieldName, tabOfField);
                    }
                    tabOfField.append(tabName).append(',');
                }
            }
            String allTabs = "";
            HashMap<String, String> fieldTabs = new HashMap<>();
            for (Map.Entry<String, StringBuilder> builderEntry : fieldTabBuilder.entrySet()) {
                StringBuilder sb = builderEntry.getValue();
                fieldTabs.put(builderEntry.getKey(), sb.deleteCharAt(sb.length() - 1).toString());
            }
            // 固定引导
            JSONObject guide = new JSONObject();
            guide.put("tabs", allTabs);
            guide.put("message", IS_SUPPORT_FAR_FIELD_VOICE ? "放下遥控器，试试说" : "长按语音键");
            // 填充
            HashMap<String, LinkedList<String>> verbalStacks = new HashMap<>();
            HashMap<String, String> lastVerbal = new HashMap<>();
            HashSet<String> finishedFields = new HashSet<>();
            JSONArray infoList = data.getJSONArray("infoList");
            JSONArray holeList = selectedSegment.getJSONArray("fieldGroupList");
            int round = 0;
            int count = -1;
            _round:
            while (true) {
                // 每循环一次代表填充一轮
                if (!verbalStacks.isEmpty() && finishedFields.size() == verbalStacks.size()) {
                    // 当所有域的话术都填充完后结束
                    break;
                }
                // 先放固定引导
                // 防止出现全是固定引导话术的情况
                if (outResult.length() > count) {
                    outResult.put(guide);
                } else {
                    // 上轮未找到话术填充
                    if (!verbalStacks.isEmpty() && finishedFields.size() == verbalStacks.size()) {
                        // 当所有域的话术都填充完后结束
                        break;
                    }
                }
                count = outResult.length();
                if (outResult.length() >= OUT_RESULT_SIZE) {
                    break;
                }
                for (int i = 0; i < holeList.length(); i++) {
                    // hole代表一个坑，一个坑可以有多个域
                    JSONArray hole = holeList.getJSONObject(i).getJSONArray("fieldGroupItemList");
                    // 首先确定要选取的域
                    String fieldName = hole.getJSONObject(round % hole.length()).getString("name");
                    if (fieldName.equals(FIELD_NAME_CURRENT_TAB)) {
                        for (Map.Entry<String, String> fieldTab : fieldTabs.entrySet()) {
                            String message = getNextMessageByField(showTimeName, showPeriod, verbalStacks, lastVerbal, finishedFields, infoList, fieldTab.getKey());
                            if (addMessage(outResult, message, fieldTab.getValue())) {
                                break _round;
                            }
                        }
                    } else {
                        String message = getNextMessageByField(showTimeName, showPeriod, verbalStacks, lastVerbal, finishedFields, infoList, fieldName);
                        if (addMessage(outResult, message, allTabs)) {
                            break _round;
                        }
                    }
                }
                round++;
            }
        }
        Logger.getDefault().v("expire time: %tT, data count: %s", finishTime, outResult.length());
        return outResult.length() <= 1 ? null : outResult;
    }

    /**
     * @param outResult
     * @param message
     * @param tabs
     * @return 是否已填满
     * @throws JSONException
     */
    private static boolean addMessage(JSONArray outResult, String message, String tabs) throws JSONException {
        if (message != null) {
            outResult.put(new JSONObject().put("message", message).put("tabs", tabs));
            if (outResult.length() >= OUT_RESULT_SIZE) {
                return true;
            }
        }
        return false;
    }

    private static String getNextMessageByField(String showTimeName, long[] showPeriod, HashMap<String, LinkedList<String>> verbalStacks,
                                                HashMap<String, String> lastVerbal, HashSet<String> finishedFields, JSONArray infoList,
                                                String fieldName) throws JSONException {
        // 获取对应域的话术栈
        LinkedList<String> verbalStack = verbalStacks.get(fieldName);
        if (verbalStack == null) {
            verbalStack = new LinkedList<>();
            verbalStacks.put(fieldName, verbalStack);
            if (!fillVerbalStack(showTimeName, infoList, showPeriod, fieldName, verbalStack)) {
                // 填充后仍然为空，跳过
                addFinishField(fieldName, finishedFields);
                return null;
            }
        }
        if (verbalStack.isEmpty()) {
            addFinishField(fieldName, finishedFields);
            // 此时填充仍可能为空
            if (!fillVerbalStack(showTimeName, infoList, showPeriod, fieldName, verbalStack)) {
                return null;
            }
        }
        // 从话术栈中取出来填充
        boolean isFilled = false;
        while (true) {
            String pop = verbalStack.pop();
            if (!pop.equals(lastVerbal.get(fieldName))) {
                lastVerbal.put(fieldName, pop);
                return pop;
            } else if (verbalStack.isEmpty()) {
                if (isFilled) {
                    // 填充过了又取完了还是没有合适的话术，返回以避免死循环
                    return null;
                }
                addFinishField(fieldName, finishedFields);
                fillVerbalStack(showTimeName, infoList, showPeriod, fieldName, verbalStack);
                isFilled = true;
                if (verbalStack.size() < 2) {
                    // 避免死循环
                    return null;
                }
            }
        }
    }

    private static void addFinishField(String fieldName, HashSet<String> finishedFields) {
        if (finishedFields.add(fieldName)) {
            Logger.getDefault().vv("finish field: ", fieldName);
        }
    }

    private static boolean fillVerbalStack(String showTime, JSONArray infoList, long[] time, String fieldName, LinkedList<String> verbalStack) throws JSONException {
        for (int k = 0; k < infoList.length(); k++) {
            JSONObject field = infoList.getJSONObject(k);
            String tags = field.optString("flag");
            // 支持全场景筛选
            if (!IS_SUPPORT_FULL_SCENE && tags.contains(FIELD_TAG_FULL_SCENE)) {
                break;
            }
            if (field.getString("name").equals(fieldName)) {
                JSONArray verbals = field.getJSONArray("infos");
                for (int l = 0; l < verbals.length(); l++) {
                    JSONObject verbal = verbals.getJSONObject(l);
                    // 按时间段筛选
                    String times = verbal.optString("occasion");
                    if (!isEmpty(times)) {
                        if (showTime == null) {
                            continue;
                        }
                        if (!times.contains(showTime)) {
                            continue;
                        }
                    }
                    // 按生效时间筛选
                    long effectiveTime = verbal.optLong("effectiveTime");
                    if (effectiveTime > 0 && effectiveTime > time[0]) {
                        continue;
                    }
                    long expireTime = verbal.optLong("expireTime");
                    if (expireTime > 0 && expireTime < time[1]) {
                        continue;
                    }
                    // 处理权重
                    int weight = verbal.optInt("weight");
                    if (weight == 0) {
                        weight = 1;
                    }
                    for (int m = 0; m < weight; m++) {
                        String info = verbal.getString("info");
                        if (IS_SUPPORT_FAR_FIELD_VOICE) {
                            info = String.format("“小T小T %s”", info);
                        } else {
                            info = String.format("%s“%s”", tags.contains(FIELD_TAG_FESTIVAL) ? "试试对小T说说" : "说", info);
                        }
                        verbalStack.add(info);
                    }
                }
                // 打散
                Collections.shuffle(verbalStack, Singletons.get(Random.class));
                break;
            }
        }
        if (verbalStack.isEmpty()) {
            return false;
        }
        return true;
    }

    private static boolean isEmpty(String string) {
        return string == null || string.length() == 0 || string.equalsIgnoreCase("null");
    }

    /**
     * @param timeName
     * @return [startTime, endTime]
     */
    private static long[] getTime(String timeName, long now) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long[] time = new long[2];
        boolean isBefore6 = calendar.get(Calendar.HOUR_OF_DAY) < 6;
        if (timeName == null) {
            if (isBefore6) {
                setTime(calendar, time, -2, 22);
            } else {
                setTime(calendar, time, 6, 30);
            }
        } else {
            // TODO 先写死时间段，后面会根据服务器返回的配置获取具体时间
            switch (timeName) {
                case "morning":
                    setTime(calendar, time, 6, 11);
                    break;
                case "afternoon":
                    setTime(calendar, time, 11, 18);
                    break;
                case "evening":
                    setTime(calendar, time, 18, 22);
                    break;
                case "night":
                    if (isBefore6) {
                        setTime(calendar, time, -2, 6);
                    } else {
                        setTime(calendar, time, 22, 30);
                    }
                    break;
            }
        }
        return time;
    }

    private static void setTime(Calendar calendar, long[] time, int startHour, int endHour) {
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        time[0] = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, endHour);
        time[1] = calendar.getTimeInMillis();
    }

    private static boolean checkFestival(long time) throws IOException, JSONException {
        Map<String, Object> params = new HashMap<>();
        String checkedDate = String.format("%tY%<tm%<td", time);
        params.put("date", checkedDate);
        HttpGet get = new HttpGet(getBaseUrl() + "speechcraft/public/newfestivalcheck", params, 0);
        get.send();
        if (get.isSuccess()) {
            String resp = get.getString("utf-8");
            JSONObject respJson = new JSONObject(resp);
            int code = respJson.optInt("code");
            if (code == 200) {
                JSONObject jsonObject = respJson.getJSONObject("data");
                JSONArray festivalDays = jsonObject.getJSONArray("festivalDays");
                for (int i = 0; i < festivalDays.length(); i++) {
                    if (festivalDays.getString(i).equals(checkedDate)) {
                        return true;
                    }
                }
                // 周末调休要上班
                JSONArray workDays = jsonObject.getJSONArray("workDays");
                for (int i = 0; i < workDays.length(); i++) {
                    if (workDays.getString(i).equals(checkedDate)) {
                        return false;
                    }
                }
            } else {
                Logger.getDefault().ee("判断是否节假日接口返回错误：", resp);
            }
        }
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(time);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY | dayOfWeek == Calendar.SUNDAY;
    }
}

package com.boyitech.logstream.core.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class GsonHelper {

    private static Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<Map<String, Object>>() {
            }.getType(),
            new JsonDeserializer<Map<String, Object>>() {
                @Override
                public Map<String, Object> deserialize(
                        JsonElement json, Type typeOfT,
                        JsonDeserializationContext context) throws JsonParseException {

                    Map<String, Object> treeMap = new HashMap<String, Object>();
                    JsonObject jsonObject = json.getAsJsonObject();
                    Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                    for (Map.Entry<String, JsonElement> entry : entrySet) {
                        treeMap.put(entry.getKey(), entry.getValue());
                    }
                    return treeMap;
                }
            }).create();


//    private static Gson gson = new Gson();

    public static String toJson(Object src) {
        String result = gson.toJson(src);
        if(result.equals("null")){
            return "{}";
        }
        return result;
    }

    public static Map<String, String> fromJson(String json) {
        return gson.fromJson(json, Map.class);
    }

    public static Map<String, Map> fromJsonMap(String json) {
        return gson.fromJson(json, Map.class);
    }

    public static <clazz> List formJson(String json) {
        List list = gson.fromJson(json, List.class);
        List<clazz> result = new ArrayList<>();
        for (Object o : list) {
            result.add((clazz) o);
        }
        return result;
    }


    public static <clazz> List formJson(List<String> jsonList) {
        List<clazz> result = new ArrayList<>();
        for (String s : jsonList) {
            clazz parse = (clazz) new JsonParser().parse(s);
            result.add(parse);
        }
        return result;
    }

    public static Gson getGson() {
        return gson;
    }
}

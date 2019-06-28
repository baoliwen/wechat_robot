package com.dosimple.robot.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gson工具类
 *
 * @author wangzd
 * @date 2017-10-12 19:02
 */

@Service
public class GsonHelper {

    private static Gson gson = null;

    static {
        if (gson == null) {
            gson = new Gson();
        }
    }
    private GsonHelper() {
    }

    public static String toJson(Object object) {
        String gsonStr = null;
        if (gson != null) {
            gsonStr = gson.toJson(object);
        }
        return gsonStr;
    }

    public static <T> T parseObject(String jsonStr, Class<T> cls) {
        T t = null;
        if (gson != null) {
            t = gson.fromJson(jsonStr, cls);
        }
        return t;
    }

    public static <T> List<T> parseArray(String jsonStr, Class<T> cls) {

        List<T> list = new ArrayList<>();
        if (null == jsonStr) {
            return null;
        }
        if (gson != null) {
            JsonArray array = new JsonParser().parse(jsonStr).getAsJsonArray();
            for(final JsonElement elem : array){
                list.add(gson.fromJson(elem, cls));
            }
        }

        return list;
    }

    public static <T> List<Map<String, T>> toListMap(String jsonStr) {
        List<Map<String, T>> list = null;
        if (gson != null) {
            list = gson.fromJson(jsonStr,
                    new TypeToken<List<Map<String, T>>>() {
                    }.getType());
        }
        return list;
    }

    public static <T> Map<String, T> toMap(String jsonStr) {
        Map<String, T> map = null;
        if (gson != null) {
            map = gson.fromJson(jsonStr, new TypeToken<Map<String, T>>() {
            }.getType());
        }
        return map;
    }
}

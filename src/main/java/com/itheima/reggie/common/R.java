package com.itheima.reggie.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

@Data
public class R<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;
    private HashMap<String, Object> map = new HashMap<>();

    public static <T> R<T> success(T object) {
        R<T> r = new R<>();
        r.data = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }


}

package com.codeashen.pattern.singleton;

/**
 * 枚举式单例
 * 《Effective Java》推荐写法，反射和序列化都不能破坏枚举单例。
 */
public enum EnumSingleton {
    INSCANCE;

    private Object data;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static EnumSingleton getInstance() {
        return INSCANCE;
    }
}

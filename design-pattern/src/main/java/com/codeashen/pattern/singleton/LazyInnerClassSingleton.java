package com.codeashen.pattern.singleton;

/**
 * 懒汉式：静态内部类
 * 兼容饿汉式单例模式内存浪费和 synchronized 的性能问题，完美屏蔽了这两个缺点。
 */
public class LazyInnerClassSingleton {
    // 使用 LazyInnerClassSingleton 的时候，默认会先初始化内部类，
    // 如果没有使用，则内部类是不加载的
    private LazyInnerClassSingleton() {
    }

    // static 方法默认是 final 的，同时使得单例空间共享，并且方法不能被重写
    public static LazyInnerClassSingleton getInstance() {
        // 在返回结果以前，一定会先加载内部类
        return LazyHolder.INSTANCE;
    }

    // 默认不加载
    private static class LazyHolder {
        private static final LazyInnerClassSingleton INSTANCE = new LazyInnerClassSingleton();
    }
}

package com.codeashen.pattern.singleton;

/**
 * 懒汉式：双重检查
 */
public class LazySingleton {
    // volatile 作用：
    // 1.保证可见性，使其他线程可以读取到最新实力；
    // 2.禁止指令重排序，防止其他线程读取到提前执行赋值指令，未初始化的实例。
    private volatile static LazySingleton instance;

    private LazySingleton() {
    }

    public static LazySingleton getInstance() {
        // 第一次检查是为了延迟初始化
        if (instance == null) {
            // 同步代码块是为了加锁防止重复初始化
            synchronized (LazySingleton.class) {
                // 第二次检查是为了防止两个线程都通过了第一次检查，停在了同步代码块外，
                // 这样两个线程会依次获取到锁进入同步代码块
                if (instance == null) {
                    instance = new LazySingleton();
                    // 1.分配对象内存
                    // 2.调用构造器方法，执行初始化
                    // 3.将对象引用赋值给变量
                }
            }
        }
        return instance;
    }
}

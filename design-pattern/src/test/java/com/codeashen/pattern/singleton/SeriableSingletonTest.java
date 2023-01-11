package com.codeashen.pattern.singleton;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SeriableSingletonTest implements Serializable {
    /**
     * 反射破环单例
     */
    @Test
    void reflect() throws Exception {
        Class<HungrySingleton> clazz = HungrySingleton.class;
        Constructor<HungrySingleton> c = clazz.getDeclaredConstructor(null);
        c.setAccessible(true);
        HungrySingleton o1 = c.newInstance();
        HungrySingleton o2 = c.newInstance();
        assertNotEquals(o1, o2);
    }

    /**
     * 序列化破坏单例
     */
    @Test
    void seriable() throws Exception {
        SeriableSingleton s1 = SeriableSingleton.getInstance();
        SeriableSingleton s2;

        // 对象输出到文件
        FileOutputStream fos = new FileOutputStream("SeriableSingleton.obj");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(s1);
        oos.flush();
        oos.close();

        // 从文件读取对象
        FileInputStream fis = new FileInputStream("SeriableSingleton.obj");
        ObjectInputStream ois = new ObjectInputStream(fis);
        s2 = (SeriableSingleton) ois.readObject();
        ois.close();

        System.out.println(s1);
        System.out.println(s2);
        assertNotEquals(s1, s2);
    }
}

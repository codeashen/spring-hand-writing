package com.codeashen.pattern.factory;

public class JavaCourse implements ICourse {
    @Override
    public void record() {
        System.out.println("录制 Java 视频");
    }
}

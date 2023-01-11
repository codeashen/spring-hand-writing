package com.codeashen.pattern.factory.abstractfactory;

public class JavaVideo implements IVideo {
    @Override
    public void record() {
        System.out.println("录制 Java 视频");
    }
}

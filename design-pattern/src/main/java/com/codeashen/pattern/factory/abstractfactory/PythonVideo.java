package com.codeashen.pattern.factory.abstractfactory;

public class PythonVideo implements IVideo {
    @Override
    public void record() {
        System.out.println("录制 Python 视频");
    }
}

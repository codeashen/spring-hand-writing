package com.codeashen.pattern.factory;

public class PythonCourse implements ICourse {
    @Override
    public void record() {
        System.out.println("录制 Python 视频");
    }
}

package com.codeashen.pattern.factory.simplefacotry;

import com.codeashen.pattern.annotation.SimpleFactoryPattern;
import com.codeashen.pattern.factory.ICourse;
import com.codeashen.pattern.factory.JavaCourse;
import com.codeashen.pattern.factory.PythonCourse;

@SimpleFactoryPattern
public class CourseFactory1 {
    public ICourse create(String name) {
        if ("java".equals(name)) {
            return new JavaCourse();
        } else if ("python".equals(name)) {
            return new PythonCourse();
        } else {
            return null;
        }
    }
}

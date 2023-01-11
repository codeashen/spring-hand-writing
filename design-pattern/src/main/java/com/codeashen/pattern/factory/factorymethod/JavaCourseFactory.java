package com.codeashen.pattern.factory.factorymethod;

import com.codeashen.pattern.factory.ICourse;
import com.codeashen.pattern.factory.JavaCourse;

public class JavaCourseFactory implements ICourseFactory {
    @Override
    public ICourse create() {
        return new JavaCourse();
    }
}

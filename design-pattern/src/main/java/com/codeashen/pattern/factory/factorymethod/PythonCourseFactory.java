package com.codeashen.pattern.factory.factorymethod;

import com.codeashen.pattern.factory.ICourse;
import com.codeashen.pattern.factory.PythonCourse;

public class PythonCourseFactory implements ICourseFactory {
    @Override
    public ICourse create() {
        return new PythonCourse();
    }
}

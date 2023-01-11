package com.codeashen.pattern.factory;

import com.codeashen.pattern.factory.factorymethod.ICourseFactory;
import com.codeashen.pattern.factory.factorymethod.JavaCourseFactory;
import com.codeashen.pattern.factory.factorymethod.PythonCourseFactory;
import org.junit.jupiter.api.Test;

public class FactoryMethodPatternTest {
    @Test
    void test1() {
        ICourseFactory factory = new PythonCourseFactory();
        ICourse course = factory.create();
        course.record();

        factory = new JavaCourseFactory();
        course = factory.create();
        course.record();
    }
}

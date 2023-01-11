package com.codeashen.pattern.factory;

import com.codeashen.pattern.factory.simplefacotry.CourseFactory1;
import com.codeashen.pattern.factory.simplefacotry.CourseFactory2;
import com.codeashen.pattern.factory.simplefacotry.CourseFactory3;
import org.junit.jupiter.api.Test;

public class SimpleFactoryPatternTest {
    @Test
    void test1() {
        CourseFactory1 factory = new CourseFactory1();
        ICourse course = factory.create("java");
        course.record();
    }

    @Test
    void test2() {
        CourseFactory2 factory = new CourseFactory2();
        ICourse course = factory.create("com.codeashen.pattern.factory.JavaCourse");
        course.record();
    }

    @Test
    void test3() {
        CourseFactory3 factory = new CourseFactory3();
        ICourse course = factory.create(JavaCourse.class);
        course.record();
    }
}

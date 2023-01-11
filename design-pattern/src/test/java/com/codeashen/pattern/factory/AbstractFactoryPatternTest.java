package com.codeashen.pattern.factory;

import com.codeashen.pattern.factory.abstractfactory.JavaCourseFactory;
import org.junit.jupiter.api.Test;

public class AbstractFactoryPatternTest {
    @Test
    void test1() {
        JavaCourseFactory factory = new JavaCourseFactory();
        factory.createNote().edit();
        factory.createVideo().record();
    }
}

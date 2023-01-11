package com.codeashen.pattern.factory.simplefacotry;

import com.codeashen.pattern.annotation.SimpleFactoryPattern;
import com.codeashen.pattern.factory.ICourse;

@SimpleFactoryPattern
public class CourseFactory3 {
    public ICourse create(Class<? extends ICourse> clazz) {
        try {
            if (null != clazz) {
                return clazz.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

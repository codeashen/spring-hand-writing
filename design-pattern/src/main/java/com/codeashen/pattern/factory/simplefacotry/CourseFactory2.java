package com.codeashen.pattern.factory.simplefacotry;

import com.codeashen.pattern.annotation.SimpleFactoryPattern;
import com.codeashen.pattern.factory.ICourse;

@SimpleFactoryPattern
public class CourseFactory2 {
    public ICourse create(String className) {
        try {
            if (className != null && !"".equals(className)) {
                return (ICourse) Class.forName(className).newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

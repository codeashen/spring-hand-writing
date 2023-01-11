package com.codeashen.pattern.factory.factorymethod;

import com.codeashen.pattern.annotation.FactoryMethodPattern;
import com.codeashen.pattern.factory.ICourse;

@FactoryMethodPattern
public interface ICourseFactory {
    ICourse create();
}

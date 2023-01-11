package com.codeashen.pattern.factory.abstractfactory;

import com.codeashen.pattern.annotation.AbstractFactoryPattern;

/**
 * 抽象工厂是用户的主入口，
 * 是 Spring 中应用得最广泛的一种设计模式，
 * 易于拓展。
 */
@AbstractFactoryPattern
public interface CourseFactory {
    INote createNote();

    IVideo createVideo();
}

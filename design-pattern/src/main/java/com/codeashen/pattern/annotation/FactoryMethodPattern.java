package com.codeashen.pattern.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 工厂方法模式
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface FactoryMethodPattern {
}

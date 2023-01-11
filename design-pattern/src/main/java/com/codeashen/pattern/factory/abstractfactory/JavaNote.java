package com.codeashen.pattern.factory.abstractfactory;

public class JavaNote implements INote {
    @Override
    public void edit() {
        System.out.println("编写 Java 笔记");
    }
}

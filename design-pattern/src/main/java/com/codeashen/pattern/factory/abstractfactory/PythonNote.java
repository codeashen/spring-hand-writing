package com.codeashen.pattern.factory.abstractfactory;

public class PythonNote implements INote {
    @Override
    public void edit() {
        System.out.println("编写 Python 笔记");
    }
}

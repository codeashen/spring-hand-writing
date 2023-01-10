package com.codeashen.demo.service.impl;

import com.codeashen.demo.service.IDemoService;
import com.codeashen.mvcframework.annotation.Service;

@Service
public class DemoService implements IDemoService {
    public String get(String name) {
        return "My name is " + name;
    }
}

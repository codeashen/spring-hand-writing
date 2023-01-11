package com.codeashen.demo.controller;

import com.codeashen.demo.service.IDemoService;
import com.codeashen.mvcframework.annotation.ASAutowired;
import com.codeashen.mvcframework.annotation.ASController;
import com.codeashen.mvcframework.annotation.ASRequestMapping;
import com.codeashen.mvcframework.annotation.ASRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ASController
@ASRequestMapping("/demo")
public class DemoController {

    @ASAutowired
    private IDemoService demoService;

    @ASRequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @ASRequestParam("name") String name) {
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ASRequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @ASRequestParam("a") Integer a, @ASRequestParam("b") Integer b) {
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ASRequestMapping("/remove")
    public void remove(HttpServletRequest req, HttpServletResponse resp,
                       @ASRequestParam("id") Integer id) {
    }
}

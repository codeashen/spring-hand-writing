package com.codeashen.mvcframework.v3.servlet;

import com.codeashen.mvcframework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 优化 2.0 版本的以下问题
 * - handleMapping 不能像 Spring MVC 一样支持正则；
 * - url 参数还不支持强制类型转换；
 * - 反射调用前还需要重新获取 beanName。
 *
 * @version 3.0
 */
public class DispatcherServlet extends HttpServlet {

    // 保存 application.properties 配置文件内容
    private final Properties contextConfig = new Properties();
    // 保存扫描到的所有类名
    private final List<String> clazzNames = new ArrayList<>();
    // IoC 容器，为了简化暂不考虑 ConcurrentHashMap，主要关注设计思想和原理
    private final Map<String, Object> ioc = new HashMap<>();
    // 保存 url 和 Method 的映射关系
    private final List<Handler> handlerMapping = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    /**
     * doPost 方法使用了委派模式，委派模式的具体逻辑在 doDispatch 方法中实现
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception, Detail: " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Handler handler = getHandler(req);
        if (handler == null) {
            resp.getWriter().write("404 Not Found!");
            return;
        }

        // 获取方法的形参列表
        Class<?>[] parameterTypes = handler.method.getParameterTypes();
        // 保存赋值参数的位置
        Object[] parameterValues = new Object[parameterTypes.length];
        // 保存请求的 url 参数列表
        Map<String, String[]> parameterMap = req.getParameterMap();

        // 设置方法参数列表
        for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
            if (!handler.paramIndexMapping.containsKey(param.getKey())) {
                continue;
            }

            // 获取参数值
            String value = Arrays.toString(param.getValue())
                    .replaceAll("\\[|\\]", "")
                    .replaceAll("\\s", ",");
            // 根据参数名获取参数索引
            int index = handler.paramIndexMapping.get(param.getKey());
            // 设置参数列表
            parameterValues[index] = convert(parameterTypes[index], value);
        }

        // 设置 HttpServletRequest 类型参数
        if (handler.paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            int index = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
            parameterValues[index] = req;
        }

        // 设置 HttpServletResponse 类型参数
        if (handler.paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            int index = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
            parameterValues[index] = resp;
        }

        // 调用方法，写出相应
        Object returnValue = handler.method.invoke(handler.controller, parameterValues);
        if (returnValue == null) {
            return;
        }
        resp.getWriter().write(returnValue.toString());
    }

    /**
     * 处理 url 的正则匹配，返回请求对应的处理器
     *
     * @param req 请求对象
     * @return 请求处理器
     * @throws Exception
     */
    private Handler getHandler(HttpServletRequest req) throws Exception {
        if (handlerMapping.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        for (Handler handler : handlerMapping) {
            try {
                Matcher matcher = handler.pattern.matcher(url);
                // 如果没匹配上，继续匹配下一个
                if (!matcher.matches()) {
                    continue;
                }
                return handler;
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }

    /**
     * 处理 url 参数的强制类型转换。
     * url 传过来的参数都是 String 类型的，由于 HTTP 基于字符串协议，只需要把 String 转为任意类型。
     *
     * @param type  目标类型
     * @param value 参数值
     * @return 转换为目标类型的对象
     */
    private Object convert(Class<?> type, String value) {
        if (Integer.class == type) {
            return Integer.valueOf(value);
        }
        // 如果还有 double 等其他类型，继续加类型判断即可，还可以使用策略模式
        return value;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1. 加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        // 2. 扫描相关类
        doScanner(contextConfig.getProperty("scanPackage"));
        // 3. 实例化扫描到的类，并将实例放入 IoC 容器
        doInstance();
        // 4. 完成依赖注入
        doAutowired();
        // 5. 初始化 handlerMapping
        initHandlerMapping();
        System.out.println("MVC Framework is init");
    }

    /**
     * 加载配置文件
     *
     * @param contextConfigLocation 配置文件路径
     */
    private void doLoadConfig(String contextConfigLocation) {
        InputStream fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 扫描相关类
     *
     * @param scanPackage 扫描的包路径
     */
    private void doScanner(String scanPackage) {
        // 包路径转为文件路径
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String clazzName = scanPackage + "." + file.getName().replace(".class", "");
                clazzNames.add(clazzName);
            }
        }
    }

    /**
     * 实例化扫描到的类，并将实例放入 IoC 容器。
     * 使用了工厂模式。
     */
    private void doInstance() {
        // 初始化，为 DI 做准备
        if (clazzNames.isEmpty()) {
            return;
        }

        try {
            for (String clazzName : clazzNames) {
                Class<?> clazz = Class.forName(clazzName);

                // 实例化添加了相关注解的类，处理 @Controller 和 @Service
                if (clazz.isAnnotationPresent(Controller.class)) {
                    // Spring 默认类名首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    // 获取自定义 beanName
                    Service service = clazz.getAnnotation(Service.class);
                    String beanName = service.value();
                    // 未定义 beanName 就使用类名首字母小写
                    if ("".equals(beanName.trim())) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }

                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    // 3. 根据类型自动赋值，这是投机取巧的方式
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The \"" + i.getName() + "\" is exists!");
                        }
                        // 把接口类型直接当成 key
                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 自动进行依赖注入
     */
    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 获取所有字段
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Autowired.class)) {
                    continue;
                }
                Autowired autowired = field.getAnnotation(Autowired.class);

                // 如果用户没有定义 beanName，默认就根据类型注入
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    // 获取接口类型作为 key
                    beanName = field.getType().getName();
                }

                // 反射机制给 bean 的字段复制
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 初始化 url 和 Method 的映射关系 handlerMapping。
     * handlerMapping 就是策略模式的应用案例。
     */
    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }
            String url = "";

            // 获取 Controller 上的 RequestMapping 注解值
            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                url = requestMapping.value();
            }

            // 获取 Method 上的 RequestMapping 注解值
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String regex = ("/" + url + requestMapping.value()).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                handlerMapping.add(new Handler(entry.getValue(), method, pattern));
                System.out.println("Mapped " + regex + ", " + method);
            }
        }
    }

    /**
     * 将类名首字母改为小写
     *
     * @param simpleName 类名
     */
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        // 大小写字母 ASCII 码相差 32，大写字母较小
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 内部类 Handler，记录 Controller 中 RequestMapping 和 Method 的对应关系
     */
    private class Handler {
        protected Object controller;  // 保存方法对应的实力
        protected Method method;      // 保存映射的方法
        protected Pattern pattern;
        protected Map<String, Integer> paramIndexMapping;  // 参数名 - 参数顺序

        public Handler(Object controller, Method method, Pattern pattern) {
            this.controller = controller;
            this.method = method;
            this.pattern = pattern;
            paramIndexMapping = new HashMap<>();
            putParamIndexMapping(method);
        }

        private void putParamIndexMapping(Method method) {
            // 提取方法中加了注解的参数
            Annotation[][] pa = method.getParameterAnnotations();
            for (int i = 0; i < pa.length; i++) {
                for (Annotation a : pa[i]) {
                    if (a instanceof RequestParam) {
                        String paramName = ((RequestParam) a).value();
                        if (!"".equals(paramName.trim())) {
                            paramIndexMapping.put(paramName, i);
                        }
                    }
                }
            }
            // 提取方法中 request 和 response 参数
            Class<?>[] paramsTypes = method.getParameterTypes();
            for (int i = 0; i < paramsTypes.length; i++) {
                Class<?> type = paramsTypes[i];
                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                    paramIndexMapping.put(type.getName(), i);
                }
            }
        }
    }

}

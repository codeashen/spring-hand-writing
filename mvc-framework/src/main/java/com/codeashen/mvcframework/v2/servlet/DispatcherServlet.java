package com.codeashen.mvcframework.v2.servlet;

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

/**
 * 在 1.0 的基础上优化，采用工厂、单例、委派、策略模式，将 init 方法中的代码进行提取封装。
 *
 * @version 2.0
 */
public class DispatcherServlet extends HttpServlet {

    // 保存 application.properties 配置文件内容
    private final Properties contextConfig = new Properties();
    // 保存扫描到的所有类名
    private final List<String> clazzNames = new ArrayList<>();
    // IoC 容器，为了简化暂不考虑 ConcurrentHashMap，主要关注设计思想和原理
    private final Map<String, Object> ioc = new HashMap<>();
    // 保存 url 和 Method 的映射关系
    private final Map<String, Method> handlerMapping = new HashMap<>();

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
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if (!handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!");
            return;
        }

        Method method = handlerMapping.get(url);
        // 获取方法的形参列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        // 保存赋值参数的位置
        Object[] parameterValues = new Object[parameterTypes.length];
        // 保存请求的 url 参数列表
        Map<String, String[]> parameterMap = req.getParameterMap();

        // 根据参数位置动态赋值
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                parameterValues[i] = req;
                continue;
            } else if (parameterType == HttpServletResponse.class) {
                parameterValues[i] = resp;
                continue;
            } else if (parameterType == String.class) {
                // 提取方法中加了注解的参数
                Annotation[][] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length; j++) {
                    for (Annotation a : pa[i]) {
                        if (a instanceof ASRequestParam) {
                            String paramName = ((ASRequestParam) a).value();
                            if (!"".equals(paramName.trim())) {
                                String value = Arrays.toString(parameterMap.get(paramName))
                                        .replaceAll("\\[|\\]", "")
                                        .replaceAll("\\s", ",");
                                parameterValues[i] = value;
                            }
                        }
                    }
                }
            }
        }

        // 调用方法
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName), parameterValues);
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
                if (clazz.isAnnotationPresent(ASController.class)) {
                    // Spring 默认类名首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(ASService.class)) {
                    // 获取自定义 beanName
                    ASService service = clazz.getAnnotation(ASService.class);
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
                if (!field.isAnnotationPresent(ASAutowired.class)) {
                    continue;
                }
                ASAutowired autowired = field.getAnnotation(ASAutowired.class);

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
            if (!clazz.isAnnotationPresent(ASController.class)) {
                continue;
            }

            // 保存写在类上的 RequestMapping 注解值
            String baseUrl = "";
            if (clazz.isAnnotationPresent(ASRequestMapping.class)) {
                ASRequestMapping requestMapping = clazz.getAnnotation(ASRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            // 默认获取所有 public 类型的方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(ASRequestMapping.class)) {
                    continue;
                }
                ASRequestMapping requestMapping = method.getAnnotation(ASRequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value())
                        .replaceAll("/+", "/");
                handlerMapping.put(url, method);
                System.out.println("Mapped " + url + ", " + method);
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

}

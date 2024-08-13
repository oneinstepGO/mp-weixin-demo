/*
 *
 */
package com.oneinstep.myspi0.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 核心类
 * 扩展点加载器
 *
 * @param <T> 扩展点类型
 */
@Slf4j
public class ExtensionLoader<T> {
    /**
     * 扩展点加载器缓存
     * key: 扩展点接口类型
     * value: 扩展点加载器
     */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS_MAP = new ConcurrentHashMap<>(64);
    /**
     * 扩展点接口类型
     */
    private final Class<?> type;
    /**
     * 缓存的扩展点实例
     */
    private final ConcurrentMap<Class<?>, Object> extensionInstances = new ConcurrentHashMap<>(64);
    /**
     * 缓存SPI接口对应的实现类Class
     * key: 扩展点名称 value: 扩展点实现类Class
     */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();
    /**
     * SPI 资源文件目录
     */
    private static final String SPI_DIRECTORY = "META-INF/my-spi0/";
    /**
     * JDK编译器，用于编译自适应扩展类，还可以使用 JavassistCompiler 等其它编译器
     */
    private static final JdkCompiler CODE_COMPILER = new JdkCompiler();
    /**
     * 缓存的自适应扩展类
     */
    private volatile Class<?> cachedAdaptiveClass = null;

    ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    public List<Object> getExtensionInstances() {
        return new ArrayList<>(extensionInstances.values());
    }

    // 根据接口类型获取扩展点加载器
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type (" + type + ") is not an interface!");
        }

        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS_MAP.get(type);

        if (loader == null) {
            EXTENSION_LOADERS_MAP.putIfAbsent(type, new ExtensionLoader<>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS_MAP.get(type);
            return loader;
        }

        return loader;
    }

    // 根据扩展点名称获取扩展点实例
    public T getExtension(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Extension name == null");
        }
        T instance = createExtension(name);
        if (instance == null) {
            throw new IllegalArgumentException("Not find extension: " + name);
        }
        return instance;
    }

    // 获取自适应扩展点实例
    @SuppressWarnings("unchecked")
    public T getAdaptiveExtension() {
        T instance;
        try {
            instance = (T) getAdaptiveExtensionClass().getDeclaredConstructor().newInstance();
        } catch (Exception t) {
            throw new IllegalStateException("Failed to create adaptive instance: " + t.toString(), t);
        }
        return instance;
    }

    // 获取自适应扩展点类
    private Class<?> getAdaptiveExtensionClass() {
        // 加载扩展点实现类
        getExtensionClasses();
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass;
        }

        // 生成自适应扩展点类
        cachedAdaptiveClass = createAdaptiveExtensionClass();
        return cachedAdaptiveClass;
    }

    // 生成自适应扩展点类
    private Class<?> createAdaptiveExtensionClass() {
        // 生成自适应扩展点代码
        String code = new AdaptiveClassCodeGenerator(type).generate();
        // 使用 JDK Compiler 编译代码
        return CODE_COMPILER.compile(code, type.getClassLoader());
    }

    @SuppressWarnings("unchecked")
    private T createExtension(String name) {
        // 获取扩展点实现类Class
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new IllegalStateException("No such extension " + name + " for " + type.getName());
        }
        try {
            // 从缓存中获取扩展点实例
            T instance = (T) extensionInstances.get(clazz);
            if (instance == null) {
                extensionInstances.putIfAbsent(clazz, clazz.getDeclaredConstructor().newInstance());
                instance = (T) extensionInstances.get(clazz);
            }
            return instance;
        } catch (Exception t) {
            throw new IllegalStateException(
                    "Extension instance (name: " + name + ", class: " + type + ") couldn't be instantiated: "
                            + t.getMessage(),
                    t);
        }
    }

    // 加载扩展点实现类
    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    try {
                        classes = new HashMap<>();
                        // 从 SPI 资源文件加载扩展点实现类
                        loadDirectoryInternal(classes, type.getName());
                    } catch (InterruptedException e) {
                        log.error("Failed to load extension classes for extension {}", type.getName(), e);
                        throw new IllegalStateException(
                                "Exception occurred when loading extension class (interface: " + type + ")", e);
                    }
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    // 从 SPI 资源文件加载扩展点实现类
    private void loadDirectoryInternal(Map<String, Class<?>> extensionClasses, String type)
            throws InterruptedException {
        String fileName = SPI_DIRECTORY + type;
        ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
        try {
            // 获取资源
            Set<URL> resources = getResources(classLoader, fileName);
            // 加载扩展点实现类
            loadFromClass(extensionClasses, resources, classLoader);
        } catch (Throwable t) {
            log.error("Exception occurred when loading extension class (interface: {}, description file: {})", type, fileName, t);
        }
    }

    // 获取资源
    private static Set<URL> getResources(ClassLoader classLoader, String fileName) {
        Set<URL> resources = new LinkedHashSet<>();
        Enumeration<URL> urls;
        try {
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    resources.add(url);
                }
            }
        } catch (IOException e) {
            log.error("Failed to load resources from {}", fileName, e);
        }
        return resources;
    }

    // 从类加载器加载扩展点实现类
    private void loadFromClass(Map<String, Class<?>> extensionClasses, Set<java.net.URL> urls, ClassLoader classLoader) {
        for (java.net.URL resourceURL : urls) {
            try {
                List<String> newContentList = getResourceContent(resourceURL);
                for (String line : newContentList) {
                    try {
                        loadLine(extensionClasses, classLoader, line);
                    } catch (Exception t) {
                        log.error("Exception occurred when loading extension class (interface: {}, class file: {})", type, resourceURL, t);
                    }
                }
            } catch (Exception e) {
                log.error("Exception occurred when loading extension class (interface: {}, class file: {})", type, resourceURL, e);
            }
        }
    }

    // 获取资源内容
    private List<String> getResourceContent(java.net.URL resourceURL) {
        List<String> newContentList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    newContentList.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return newContentList;
    }

    // 加载每一行的扩展点实现类
    private void loadLine(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, String line) throws ClassNotFoundException {
        // 解析出 name 和 clazz
        String clazzName;
        String name = null;
        int i = line.indexOf('=');
        if (i > 0) {
            name = line.substring(0, i).trim();
            clazzName = line.substring(i + 1).trim();
        } else {
            clazzName = line;
        }

        if (StringUtils.isNotEmpty(clazzName)) {
            // 使用 ClassLoader 加载类
            Class<?> clazz = Class.forName(clazzName, true, classLoader);
            if (!type.isAssignableFrom(clazz)) {
                throw new IllegalStateException(
                        "Error occurred when loading extension class (interface: " + type + ", class line: "
                                + clazz.getName() + "), class " + clazz.getName() + " is not subtype of interface.");
            }

            Class<?> c = extensionClasses.get(name);
            if (c == null) {
                extensionClasses.put(name, clazz);
            } else if (c != clazz) {
                // 多个实现不可以重名
                String duplicateMsg = "Duplicate extension " + type.getName() + " name " + name + " on " + c.getName()
                        + " and " + clazz.getName();
                log.error(duplicateMsg);
                throw new IllegalStateException(duplicateMsg);
            }
        }
    }


    static class Holder<T> {

        private volatile T value;

        public void set(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

    }

}

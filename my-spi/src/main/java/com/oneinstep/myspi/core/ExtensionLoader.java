/*
 *
 */
package com.oneinstep.myspi.core;

import com.oneinstep.myspi.core.compile.AdaptiveClassCodeGenerator;
import com.oneinstep.myspi.core.compile.CodeCompiler;
import com.oneinstep.myspi.core.compile.JavassistCodeCompiler;
import com.oneinstep.myspi.core.inject.ExtensionInjector;
import com.oneinstep.myspi.core.inject.SpiExtensionInjector;
import com.oneinstep.myspi.core.utils.ClassLoaderResourceLoader;
import com.oneinstep.myspi.core.utils.ClassUtils;
import com.oneinstep.myspi.core.utils.ConcurrentHashSet;
import com.oneinstep.myspi.core.utils.Holder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * 核心类
 * 扩展点加载器
 *
 * @param <T> 扩展点类型
 */
@Slf4j
public class ExtensionLoader<T> {

    /**
     * ExtensionLoader 扩展点加载器缓存
     * key: 扩展点接口类型
     * value: ExtensionLoader 实例
     */
    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS_MAP = new ConcurrentHashMap<>(64);

    /**
     * 扩展点接口类型
     */
    private final Class<?> type;

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*,+\\s*");

    /**
     * 缓存的扩展点实例
     */
    private final ConcurrentMap<Class<?>, Object> extensionInstances = new ConcurrentHashMap<>(64);

    /**
     * 缓存的扩展点类
     */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    /**
     * 缓存的自适应扩展类
     */
    private volatile Class<?> cachedAdaptiveClass = null;
    /**
     * 缓存的默认扩展名
     */
    private String cachedDefaultName;

    /**
     * 扩展点注入器
     */
    private final ExtensionInjector injector;

    /**
     * 记录加载扩展点类时发生的异常
     */
    private final Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<>();

    /**
     * URL 到内容列表的缓存
     */
    private static SoftReference<Map<java.net.URL, List<String>>> urlListMapCache =
            new SoftReference<>(new ConcurrentHashMap<>());

    /**
     * SPI 目录
     */
    private static final String SPI_DIRECTORY = "META-INF/my-spi/";

    /**
     * 记录不可接受的异常
     */
    private final Set<String> unacceptableExceptions = new ConcurrentHashSet<>();

    /**
     * ExtensionPostProcessor 集合
     */
    private final List<ExtensionPostProcessor> extensionPostProcessors = new ArrayList<>();

    /**
     * 是否已销毁
     */
    private static final AtomicBoolean DESTROYED = new AtomicBoolean();

    /**
     * 编译器
     */
    private static final CodeCompiler CODE_COMPILER = new JavassistCodeCompiler();

    ExtensionLoader(Class<?> type) {
        this.type = type;
        this.injector = (type == ExtensionInjector.class
                ? null
                : new SpiExtensionInjector());
    }

    /**
     * 添加 ExtensionPostProcessor
     *
     * @param processor ExtensionPostProcessor
     */
    public void addExtensionPostProcessor(ExtensionPostProcessor processor) {
        if (!this.extensionPostProcessors.contains(processor)) {
            this.extensionPostProcessors.add(processor);
        }
    }

    @SuppressWarnings("unchecked")
    public T getExtension(String name) {
        checkDestroyed();
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Extension name == null");
        }
        if ("true".equals(name)) {
            return getDefaultExtension();
        }
        T instance = createExtension(name);

        if (instance == null) {
            throw new IllegalArgumentException("Not find extension: " + name);
        }
        return instance;
    }

    public static List<String> getAllExtensionLoaderTypes() {
        List<String> names = new ArrayList<>();
        EXTENSION_LOADERS_MAP.forEach((type, loader) -> names.add(type.getSimpleName()));
        return names;
    }

    public List<String> getCachedClassesName() {
        Map<String, Class<?>> classMap = cachedClasses.get();
        if (classMap == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(classMap.keySet());
    }

    public List<Object> getExtensionInstances() {
        return new ArrayList<>(extensionInstances.values());
    }

    /**
     * 销毁 ExtensionLoader
     */
    public static void destroy() {
        if (!DESTROYED.compareAndSet(false, true)) {
            return;
        }

        EXTENSION_LOADERS_MAP.forEach((type, loader) -> {
            if (loader != null) {
                EXTENSION_LOADERS_MAP.remove(type);
                loader.extensionInstances.clear();
                loader.cachedAdaptiveClass = null;
                loader.cachedDefaultName = null;
                loader.cachedClasses.set(null);
                loader.exceptions.clear();
                loader.unacceptableExceptions.clear();
                loader.extensionPostProcessors.clear();
            }
        });

    }

    /**
     * 获取扩展点加载器
     *
     * @param type 扩展点接口类型
     * @param <T>  扩展点类型
     * @return ExtensionLoader 实例
     */
    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type == null");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type (" + type + ") is not an interface!");
        }

        // 1. find in local cache
        ExtensionLoader<T> loader = (ExtensionLoader<T>) EXTENSION_LOADERS_MAP.get(type);

        // 2. find in parent

        // 3. create it
        if (loader == null) {
            checkDestroyed();
            log.info("createExtensionLoader: {}", type);
            EXTENSION_LOADERS_MAP.putIfAbsent(type, new ExtensionLoader<>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS_MAP.get(type);
            return loader;
        }

        return loader;
    }

    /**
     * Return default extension, return <code>null</code> if it's not configured.
     */
    public T getDefaultExtension() {
        getExtensionClasses();
        if (cachedDefaultName == null || cachedDefaultName.isEmpty() || "true".equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }

    public Set<String> getSupportedExtensions() {
        checkDestroyed();
        Map<String, Class<?>> classes = getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<>(classes.keySet()));
    }

    @SuppressWarnings("unchecked")
    public T getAdaptiveExtension() {
        checkDestroyed();
        T instance;
        try {
            instance = createAdaptiveExtension();
        } catch (Exception t) {
            throw new IllegalStateException("Failed to create adaptive instance: " + t.toString(), t);
        }
        return instance;
    }

    private IllegalStateException findException(String name) {
        StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name);

        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (entry.getKey().toLowerCase().startsWith(name.toLowerCase())) {
                if (i == 1) {
                    buf.append(", possible causes: ");
                }
                buf.append("\r\n(");
                buf.append(i++);
                buf.append(") ");
                buf.append(entry.getKey());
                buf.append(":\r\n");
                buf.append(StringUtils.join(
                        Arrays.asList(entry.getValue().getStackTrace()).subList(0, 5),
                        "\r\n"));
            }
        }

        if (i == 1) {
            buf.append(", no related exception was found, please check whether related SPI module is missing.");
        }
        return new IllegalStateException(buf.toString());
    }

    @SuppressWarnings("unchecked")
    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null || unacceptableExceptions.contains(name)) {
            throw findException(name);
        }
        try {
            T instance = (T) extensionInstances.get(clazz);
            if (instance == null) {
                extensionInstances.putIfAbsent(clazz, clazz.getDeclaredConstructor().newInstance());
                instance = (T) extensionInstances.get(clazz);
                instance = postProcessBeforeInitialization(instance, name);
                injectExtension(instance);
                instance = postProcessAfterInitialization(instance, name);
            }

            return instance;
        } catch (Exception t) {
            throw new IllegalStateException(
                    "Extension instance (name: " + name + ", class: " + type + ") couldn't be instantiated: "
                            + t.getMessage(),
                    t);
        }
    }

    @SuppressWarnings("unchecked")
    private T postProcessBeforeInitialization(T instance, String name) throws Exception {
        for (ExtensionPostProcessor processor : extensionPostProcessors) {
            instance = (T) processor.postProcessBeforeInitialization(instance, name);
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private T postProcessAfterInitialization(T instance, String name) throws Exception {
        for (ExtensionPostProcessor processor : extensionPostProcessors) {
            instance = (T) processor.postProcessAfterInitialization(instance, name);
        }
        return instance;
    }

    private T injectExtension(T instance) {
        if (injector == null) {
            return instance;
        }

        try {
            for (Method method : instance.getClass().getMethods()) {

                if (!isSetter(method)) {
                    continue;
                }

                Class<?> pt = method.getParameterTypes()[0];
                if (ClassUtils.isPrimitives(pt)) {
                    continue;
                }

                try {
                    String property = getSetterProperty(method);
                    Object object = injector.getInstance(pt, property);
                    if (object != null) {
                        method.invoke(instance, object);
                    }
                } catch (Exception e) {
                    log.error("Failed to inject via method {} of interface {}: {}", method.getName(), type.getName(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to inject via method in class {}: {}", instance.getClass(), e.getMessage(), e);
        }
        return instance;
    }

    /**
     * get properties name for setter, for instance: setVersion, return "version"
     * <p>
     * return "", if setter name with length less than 3
     */
    private String getSetterProperty(Method method) {
        return method.getName().length() > 3
                ? method.getName().substring(3, 4).toLowerCase()
                + method.getName().substring(4)
                : "";
    }

    /**
     * check if the method is a setter
     */
    private boolean isSetter(Method method) {
        return method.getName().startsWith("set") && method.getParameterTypes().length == 1 && Modifier.isPublic(method.getModifiers());
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    try {
                        classes = loadExtensionClasses();
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

    /**
     * synchronized in getExtensionClasses
     */
    private Map<String, Class<?>> loadExtensionClasses() throws InterruptedException {
        checkDestroyed();
        cacheDefaultExtensionName();
        Map<String, Class<?>> extensionClasses = new HashMap<>();
        loadDirectoryInternal(extensionClasses, type.getName());
        return extensionClasses;
    }

    /**
     * extract and cache default extension name if exists
     */
    private void cacheDefaultExtensionName() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation == null) {
            return;
        }

        String value = defaultAnnotation.value();
        value = value.trim();
        if (StringUtils.isNotEmpty(value)) {
            cachedDefaultName = value;
        }
    }

    private void loadDirectoryInternal(
            Map<String, Class<?>> extensionClasses, String type)
            throws InterruptedException {
        String fileName = SPI_DIRECTORY + type;
        try {

            // load from scope model
            Set<ClassLoader> classLoaders = new HashSet<>();
            classLoaders.add(ExtensionLoader.class.getClassLoader());

            List<ClassLoader> classLoadersToLoad = new LinkedList<>(classLoaders);

            Map<ClassLoader, Set<java.net.URL>> resources = ClassLoaderResourceLoader.loadResources(fileName, classLoadersToLoad);
            resources.forEach(((classLoader, urls) -> loadFromClass(extensionClasses, urls, classLoader)));
        } catch (InterruptedException e) {
            throw e;
        } catch (Throwable t) {
            log.error(
                    "Exception occurred when loading extension class (interface: " + type + ", description file: "
                            + fileName + ")",
                    t);
        }
    }

    private void loadFromClass(Map<String, Class<?>> extensionClasses, Set<java.net.URL> urls, ClassLoader classLoader) {
        if (CollectionUtils.isNotEmpty(urls)) {
            for (java.net.URL url : urls) {
                loadResource(extensionClasses, classLoader, url);
            }
        }
    }

    private void loadResource(
            Map<String, Class<?>> extensionClasses,
            ClassLoader classLoader,
            java.net.URL resourceURL) {
        try {
            List<String> newContentList = getResourceContent(resourceURL);

            for (String line : newContentList) {
                try {
                    loadLine(extensionClasses, classLoader, line);
                } catch (Exception t) {
                    IllegalStateException e = new IllegalStateException(
                            "Failed to load extension class (interface: " + type + ", class line: " + line + ") in "
                                    + resourceURL + ", cause: " + t.getMessage(),
                            t);
                    exceptions.put(line, e);
                }
            }
        } catch (Exception e) {
            log.error("Exception occurred when loading extension class (interface: {}, class file: {})", type, resourceURL, e);
        }
    }

    private void loadLine(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, String line) throws ClassNotFoundException {
        String clazz;
        String name = null;
        int i = line.indexOf('=');
        if (i > 0) {
            name = line.substring(0, i).trim();
            clazz = line.substring(i + 1).trim();
        } else {
            clazz = line;
        }
        if (StringUtils.isNotEmpty(clazz)) {
            loadClass(extensionClasses, Class.forName(clazz, true, classLoader), name);
        }
    }

    private List<String> getResourceContent(java.net.URL resourceURL) {
        Map<java.net.URL, List<String>> urlListMap = urlListMapCache.get();
        if (urlListMap == null) {
            synchronized (ExtensionLoader.class) {
                if ((urlListMap = urlListMapCache.get()) == null) {
                    urlListMap = new ConcurrentHashMap<>();
                    urlListMapCache = new SoftReference<>(urlListMap);
                }
            }
        }

        return urlListMap.computeIfAbsent(resourceURL, key -> {
            List<String> newContentList = new ArrayList<>();

            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8))) {
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
        });
    }

    private void loadClass(
            Map<String, Class<?>> extensionClasses,
            Class<?> clazz,
            String name) {
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException(
                    "Error occurred when loading extension class (interface: " + type + ", class line: "
                            + clazz.getName() + "), class " + clazz.getName() + " is not subtype of interface.");
        }

        if (clazz.isAnnotationPresent(Adaptive.class)) {
            cacheAdaptiveClass(clazz);
        } else {
            String[] names = NAME_SEPARATOR.split(name);
            if (ArrayUtils.isNotEmpty(names)) {
                for (String n : names) {
                    saveInExtensionClass(extensionClasses, clazz, n);
                }
            }
        }
    }

    /**
     * put clazz in extensionClasses
     */
    private void saveInExtensionClass(
            Map<String, Class<?>> extensionClasses, Class<?> clazz, String name) {
        Class<?> c = extensionClasses.get(name);
        if (c == null) {
            extensionClasses.put(name, clazz);
        } else if (c != clazz) {
            // 多个实现不可以重名
            unacceptableExceptions.add(name);
            String duplicateMsg = "Duplicate extension " + type.getName() + " name " + name + " on " + c.getName()
                    + " and " + clazz.getName();
            log.error(duplicateMsg);
            throw new IllegalStateException(duplicateMsg);
        }
    }

    /**
     * cache Adaptive class which is annotated with <code>Adaptive</code>
     */
    private void cacheAdaptiveClass(Class<?> clazz) {
        if (cachedAdaptiveClass == null) {
            cachedAdaptiveClass = clazz;
        } else if (!cachedAdaptiveClass.equals(clazz)) {
            throw new IllegalStateException(
                    "More than 1 adaptive class found: " + cachedAdaptiveClass.getName() + ", " + clazz.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private T createAdaptiveExtension() {
        try {
            T instance = (T) getAdaptiveExtensionClass().getDeclaredConstructor().newInstance();
            instance = postProcessBeforeInitialization(instance, null);
            injectExtension(instance);
            instance = postProcessAfterInitialization(instance, null);
            return instance;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Can't create adaptive extension " + type + ", cause: " + e.getMessage(), e);
        }
    }

    private Class<?> getAdaptiveExtensionClass() {
        getExtensionClasses();
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass;
        }

        cachedAdaptiveClass = createAdaptiveExtensionClass();
        return cachedAdaptiveClass;
    }

    private Class<?> createAdaptiveExtensionClass() {
        // Adaptive Classes' ClassLoader should be the same with Real SPI interface classes' ClassLoader
        ClassLoader classLoader = type.getClassLoader();
        log.info("classLoader: {}", classLoader);
        String code = new AdaptiveClassCodeGenerator(type, cachedDefaultName).generate();
        // 使用 JDK Compiler 编译代码
        return CODE_COMPILER.compile(code, classLoader);
    }

    private static void checkDestroyed() {
        if (DESTROYED.get()) {
            throw new IllegalStateException("ExtensionLoaders have been destroyed!");
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + type.getName() + "]";
    }

}

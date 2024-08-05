package com.oneinstep.myspi.core.compile;

import com.oneinstep.myspi.core.utils.ClassUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 抽象代码编译器
 */
public abstract class AbsCodeCompiler implements CodeCompiler {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([$_a-zA-Z][$_a-zA-Z0-9.]*);");
    private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+");
    // 类创建中的锁
    protected static final ConcurrentHashMap<String, Lock> CLASS_IN_CREATION_MAP = new ConcurrentHashMap<>();

    @Override
    public Class<?> compile(String sourceCode, ClassLoader classLoader) {
        sourceCode = sourceCode.trim();
        // 获取类名
        String name = getClassName(sourceCode);
        // 获取类创建中的锁
        Lock lock = CLASS_IN_CREATION_MAP.get(name);
        if (lock == null) {
            // 如果没有锁则创建一个
            CLASS_IN_CREATION_MAP.putIfAbsent(name, new ReentrantLock());
            lock = CLASS_IN_CREATION_MAP.get(name);
        }
        try {
            // 加锁
            lock.lock();
            // 尝试获取已经加载的类
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            // 没有加载 则使用源码编译
            if (!sourceCode.endsWith("}")) {
                throw new IllegalStateException("The java code not endsWith \"}\", code: \n" + sourceCode + "\n");
            }

            int i = name.lastIndexOf('.');
            // 类名
            String className = i < 0 ? name : name.substring(i + 1);

            try {
                return doCompile(sourceCode, className, classLoader);
            } catch (Exception t) {
                throw new IllegalStateException("Failed to compile class, cause: " + t.getMessage() + ", class: "
                        + className + ", code: \n" + sourceCode + "\n, stack: " + ClassUtils.toString(t));
            }
        } finally {
            // 释放锁
            lock.unlock();
        }
    }

    protected static String getClassName(String code) {
        Matcher matcher = PACKAGE_PATTERN.matcher(code);
        String pkg;
        if (matcher.find()) {
            pkg = matcher.group(1);
        } else {
            pkg = "";
        }
        matcher = CLASS_PATTERN.matcher(code);
        String cls;
        if (matcher.find()) {
            cls = matcher.group(1);
        } else {
            throw new IllegalArgumentException("No such class name in " + code);
        }
        return pkg != null && !pkg.isEmpty() ? pkg + "." + cls : cls;
    }

    /**
     * 执行编译 模板方法 由子类实现
     *
     * @param sourceCode  源码
     * @param className   类名
     * @param classLoader 类加载器
     * @return 编译后的类
     * @throws Exception 异常
     */
    protected abstract Class<?> doCompile(String sourceCode, String className, ClassLoader classLoader) throws Exception;
}

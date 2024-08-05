package com.oneinstep.myspi.core.compile;

/**
 * 代码编译器
 */
public interface CodeCompiler {

    /**
     * 编译代码
     *
     * @param sourceCode  源码
     * @param classLoader 类加载器
     * @return 编译后的类
     */
    Class<?> compile(String sourceCode, ClassLoader classLoader);
}

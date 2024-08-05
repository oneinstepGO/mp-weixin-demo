package com.oneinstep.myspi.core.compile;

public interface Compiler {

    Class<?> compile(String sourceCode, ClassLoader classLoader);
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oneinstep.myspi.core.utils;


import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

/**
 * Class 工具类
 */
@UtilityClass
@Slf4j
public class ClassUtils {

    public static final String CLASS_EXTENSION = ".class";

    public static final String JAVA_EXTENSION = ".java";

    public static URI toURI(String name) {
        try {
            return new URI(name);
        } catch (URISyntaxException e) {
            log.error("toURI error", e);
        }
        return null;
    }

    public static String toString(Throwable e) {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        try (p) {
            p.print(e.getClass().getName() + ": ");
            if (e.getMessage() != null) {
                p.print(e.getMessage() + "\n");
            }
            p.println();
            e.printStackTrace(p);
            return w.toString();
        }
    }

    public static boolean isPrimitives(Class<?> cls) {
        while (cls.isArray()) {
            cls = cls.getComponentType();
        }
        return isPrimitive(cls);
    }

    public static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive()
                || cls == String.class
                || cls == Boolean.class
                || cls == Character.class
                || Number.class.isAssignableFrom(cls)
                || Date.class.isAssignableFrom(cls);
    }

    public static Class<?> forName(String[] packages, String className) {
        try {
            return classForName(className);
        } catch (ClassNotFoundException e) {
            if (packages != null) {
                for (String pkg : packages) {
                    try {
                        return classForName(pkg + "." + className);
                    } catch (ClassNotFoundException ignore) {
                    }
                }
            }
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Class<?> forName(String className) {
        try {
            return classForName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Class<?> classForName(String className) throws ClassNotFoundException {
        switch (className) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "char":
                return char.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "boolean[]":
                return boolean[].class;
            case "byte[]":
                return byte[].class;
            case "char[]":
                return char[].class;
            case "short[]":
                return short[].class;
            case "int[]":
                return int[].class;
            case "long[]":
                return long[].class;
            case "float[]":
                return float[].class;
            case "double[]":
                return double[].class;
            default:
        }
        try {
            return arrayForName(className);
        } catch (ClassNotFoundException e) {
            // try to load from java.lang package
            if (className.indexOf('.') == -1) {
                try {
                    return arrayForName("java.lang." + className);
                } catch (ClassNotFoundException ignore) {
                    // ignore, let the original exception be thrown
                }
            }
            throw e;
        }
    }

    private static Class<?> arrayForName(String className) throws ClassNotFoundException {
        return Class.forName(
                className.endsWith("[]") ? "[L" + className.substring(0, className.length() - 2) + ";" : className,
                true,
                Thread.currentThread().getContextClassLoader());
    }

    /**
     * get simple class name from qualified class name
     */
    public static String getSimpleClassName(String qualifiedName) {
        if (null == qualifiedName) {
            return null;
        }
        int i = qualifiedName.lastIndexOf('.');
        return i < 0 ? qualifiedName : qualifiedName.substring(i + 1);
    }
}

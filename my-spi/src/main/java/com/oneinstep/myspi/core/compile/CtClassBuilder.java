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
package com.oneinstep.myspi.core.compile;

import com.oneinstep.myspi.core.utils.ClassUtils;
import javassist.*;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CtClassBuilder is builder for CtClass
 * <p>
 * contains all the information, including:
 * <p>
 * class name, imported packages, super class name, implemented interfaces, constructors, fields, methods.
 */
public class CtClassBuilder {

    @Setter
    private String className;

    private String superClassName = "java.lang.Object";

    private final List<String> imports = new ArrayList<>();

    private final Map<String, String> fullNames = new HashMap<>();

    private final List<String> ifaces = new ArrayList<>();

    private final List<String> constructors = new ArrayList<>();

    private final List<String> fields = new ArrayList<>();

    private final List<String> methods = new ArrayList<>();

    public void setSuperClassName(String superClassName) {
        this.superClassName = getQualifiedClassName(superClassName);
    }

    public void addImports(String pkg) {
        int pi = pkg.lastIndexOf('.');
        if (pi > 0) {
            String pkgName = pkg.substring(0, pi);
            this.imports.add(pkgName);
            if (!pkg.endsWith(".*")) {
                fullNames.put(pkg.substring(pi + 1), pkg);
            }
        }
    }

    public void addInterface(String iface) {
        this.ifaces.add(getQualifiedClassName(iface));
    }

    public void addConstructor(String constructor) {
        this.constructors.add(constructor);
    }

    public void addField(String field) {
        this.fields.add(field);
    }

    public void addMethod(String method) {
        this.methods.add(method);
    }

    /**
     * get full qualified class name
     *
     * @param className super class name, maybe qualified or not
     */
    protected String getQualifiedClassName(String className) {
        if (className.contains(".")) {
            return className;
        }

        if (fullNames.containsKey(className)) {
            return fullNames.get(className);
        }

        return ClassUtils.forName(imports.toArray(new String[0]), className).getName();
    }

    /**
     * build CtClass object
     */
    public CtClass build(ClassLoader classLoader) throws NotFoundException, CannotCompileException {
        ClassPool pool = new ClassPool(true);
        pool.insertClassPath(new LoaderClassPath(classLoader));

        // create class
        CtClass ctClass = pool.makeClass(className, pool.get(superClassName));

        // add imported packages
        imports.forEach(pool::importPackage);

        // add implemented interfaces
        for (String iface : ifaces) {
            ctClass.addInterface(pool.get(iface));
        }

        // add constructors
        for (String constructor : constructors) {
            ctClass.addConstructor(CtNewConstructor.make(constructor, ctClass));
        }

        // add fields
        for (String field : fields) {
            ctClass.addField(CtField.make(field, ctClass));
        }

        // add methods
        for (String method : methods) {
            ctClass.addMethod(CtNewMethod.make(method, ctClass));
        }

        return ctClass;
    }

}

package com.oneinstep.myspi.core.compile;

import javassist.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Javassist 代码编译器
 * 简洁、性能高
 */
public class JavassistCodeCompiler extends AbsCodeCompiler {

    @Override
    protected Class<?> doCompile(String sourceCode, String className, ClassLoader classLoader) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        pool.appendClassPath(new LoaderClassPath(classLoader));
        CtClass ctClass = pool.makeClass(className);
        Matcher matcher = Pattern.compile("public\\s+\\S+\\s+" + className + "\\s*\\(").matcher(sourceCode);
        if (matcher.find()) {
            int methodStart = matcher.start();
            String classBody = sourceCode.substring(methodStart);
            CtMethod method = CtNewMethod.make(classBody, ctClass);
            ctClass.addMethod(method);
        }
        return ctClass.toClass(classLoader, null);
    }

}

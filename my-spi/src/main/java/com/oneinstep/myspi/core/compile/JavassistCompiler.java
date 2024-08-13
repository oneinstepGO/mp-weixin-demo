package com.oneinstep.myspi.core.compile;

import com.oneinstep.myspi.core.utils.ClassUtils;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavassistCompiler extends AbstractCompiler {

    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w.*]+);\n");

    private static final Pattern EXTENDS_PATTERN = Pattern.compile("\\s+extends\\s+([\\w.]+)[^{]*\\{\n");

    private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("\\s+implements\\s+([\\w.]+)\\s*\\{\n");

    private static final Pattern METHODS_PATTERN = Pattern.compile("\n(private|public|protected)\\s+");

    private static final Pattern FIELD_PATTERN = Pattern.compile("[^\n]+=[^\n]+;");

    @Override
    public Class<?> doCompile(String sourceCode, String name, ClassLoader classLoader) throws Exception {
        CtClassBuilder builder = new CtClassBuilder();
        builder.setClassName(name);

        // process imported classes
        Matcher matcher = IMPORT_PATTERN.matcher(sourceCode);
        while (matcher.find()) {
            builder.addImports(matcher.group(1).trim());
        }

        // process extended super class
        matcher = EXTENDS_PATTERN.matcher(sourceCode);
        if (matcher.find()) {
            builder.setSuperClassName(matcher.group(1).trim());
        }

        // process implemented interfaces
        matcher = IMPLEMENTS_PATTERN.matcher(sourceCode);
        if (matcher.find()) {
            String[] ifaces = matcher.group(1).trim().split(",");
            Arrays.stream(ifaces).forEach(i -> builder.addInterface(i.trim()));
        }

        // process constructors, fields, methods
        String body = sourceCode.substring(sourceCode.indexOf('{') + 1, sourceCode.length() - 1);
        String[] methods = METHODS_PATTERN.split(body);
        String className = ClassUtils.getSimpleClassName(name);
        Arrays.stream(methods).map(String::trim).filter(m -> !m.isEmpty()).forEach(method -> {
            if (method.startsWith(className)) {
                builder.addConstructor("public " + method);
            } else if (FIELD_PATTERN.matcher(method).matches()) {
                builder.addField("private " + method);
            } else {
                builder.addMethod("public " + method);
            }
        });

        // compile
        CtClass cls = builder.build(classLoader);

        ClassPool cp = cls.getClassPool();
        if (classLoader == null) {
            classLoader = cp.getClassLoader();
        }
        cp.insertClassPath(new LoaderClassPath(classLoader));

        try {
            return cp.toClass(cls, classLoader, JavassistCompiler.class.getProtectionDomain());
        } catch (Exception t) {
            if (!(t instanceof CannotCompileException)) {
                return cp.toClass(cls, classLoader, JavassistCompiler.class.getProtectionDomain());
            }
            throw t;
        }
    }
}

package com.oneinstep.apt;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * 自定义的注解处理器，用于根据自定义注解生成 getter 方法。
 * 该处理器处理被 @MyGetter 注解标注的类。
 **/
@SupportedAnnotationTypes("com.oneinstep.apt.MyGetter") // 指定该处理器要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class MyGetterProcessor extends AbstractProcessor {

    /**
     * 用来在编译期打log用的
     */
    private Messager messager;

    /**
     * 获取编译阶段的一些环境信息
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

    /**
     * 对 AST 进行处理
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 获取被自定义 MyGetter 注解修饰的元素
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(MyGetter.class);
        for (Element element : elements) {
            if (element.getKind().isClass()) {
                TypeElement classElement = (TypeElement) element;
                generateGetterMethods(classElement);
            }
        }
        return true;
    }

    /**
     * 为类生成 getter 方法
     */
    private void generateGetterMethods(TypeElement classElement) {
        String packageName = processingEnv.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
        String simpleClassName = classElement.getSimpleName().toString();
        String generatedClassName = simpleClassName + "Getters";

        try {
            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(packageName + "." + generatedClassName);
            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                out.println("package " + packageName + ";");
                out.println();
                out.println("public class " + generatedClassName + " {");
                out.println();
                for (VariableElement field : ElementFilter.fieldsIn(classElement.getEnclosedElements())) {
                    if (field.getModifiers().contains(Modifier.PRIVATE)) {
                        String fieldName = field.getSimpleName().toString();
                        TypeMirror fieldType = field.asType();
                        String methodName = getNewMethodName(fieldName);
                        out.println("    public " + fieldType + " " + methodName + "() {");
                        out.println("        return this." + fieldName + ";");
                        out.println("    }");
                        out.println();
                    }
                }
                out.println("}");
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.toString());
        }
    }

    /**
     * 驼峰命名法
     */
    private String getNewMethodName(String fieldName) {
        return "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}

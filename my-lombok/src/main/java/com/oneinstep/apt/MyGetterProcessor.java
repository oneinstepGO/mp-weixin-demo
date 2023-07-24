package com.oneinstep.apt;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * 自定义的注解处理器，用于根据自定义注解生成 getter 方法。
 * 该处理器处理被 @MyGetter 注解标注的类。
 *
 * @author aaron.shaw
 * @date 2023-07-21 01:05
 **/
@SupportedAnnotationTypes({"com.oneinstep.apt.MyGetter"}) // 指定该处理器要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MyGetterProcessor extends AbstractProcessor {

    /**
     * 用来在编译期打log用的
     */
    protected Messager messager;
    /**
     * 提供了待处理的抽象语法树
     */
    protected JavacTrees trees;
    /**
     * 封装了创建AST节点的一些方法
     */
    protected TreeMaker treeMaker;
    /**
     * 提供了创建标识符的方法
     */
    protected Names names;

    /**
     * 获取编译阶段的一些环境信息
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        processingEnv = jbUnwrap(ProcessingEnvironment.class, processingEnv);
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }


    /**
     * 对 AST 进行处理
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 获取被自定义 MyGetter 注解修饰的元素
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(MyGetter.class);
        elements.forEach(element -> {
            // 根据元素获取对应的语法树 JCTree
            JCTree jcTree = trees.getTree(element);
            jcTree.accept(new TreeTranslator() {
                // 处理语法树的类定义部分 JCClassDecl
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    List<JCTree.JCVariableDecl> jcVariableDeclList = List.nil();
                    for (JCTree varTree : jcClassDecl.defs) {
                        // 找到语法树上的成员变量节点，存储到 jcVariableDeclList 集合
                        if (varTree.getKind().equals(Tree.Kind.VARIABLE)) {
                            JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) varTree;
                            jcVariableDeclList = jcVariableDeclList.append(jcVariableDecl);
                        }
                    }
                    // 为成员变量构造 getter 方法，并添加到 JCClassDecl 之中
                    jcVariableDeclList.forEach(jcVariableDecl -> {
                        messager.printMessage(Diagnostic.Kind.NOTE, jcVariableDecl.getName() + " has been processed");
                        jcClassDecl.defs = jcClassDecl.defs.prepend(makeGetterMethodDecl(jcVariableDecl));
                    });
                    super.visitClassDef(jcClassDecl);
                }

            });
        });
        return true;
    }


    /**
     * 为成员遍历构造 getter 方法
     */
    private JCTree.JCMethodDecl makeGetterMethodDecl(JCTree.JCVariableDecl jcVariableDecl) {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        // 生成表达式 return this.value;
        statements.append(treeMaker.Return(treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl.getName())));
        // 加上大括号 { return this.value; }
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        // 组装方法
        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), getNewMethodName(jcVariableDecl.getName()), jcVariableDecl.vartype, List.nil(), List.nil(), List.nil(), body, null);
    }

    /**
     * 驼峰命名法
     */
    private Name getNewMethodName(Name name) {
        String s = name.toString();
        return names.fromString("get" + s.substring(0, 1).toUpperCase() + s.substring(1, name.length()));
    }

    /**
     * 获取 IDEA 环境下的 ProcessingEnvironment
     */
    private static <T> T jbUnwrap(Class<? extends T> iface, T wrapper) {
        T unwrapped = null;
        try {
            final Class<?> apiWrappers = wrapper.getClass().getClassLoader().loadClass("org.jetbrains.jps.javac.APIWrappers");
            final Method unwrapMethod = apiWrappers.getDeclaredMethod("unwrap", Class.class, Object.class);
            unwrapped = iface.cast(unwrapMethod.invoke(null, iface, wrapper));
        } catch (Throwable ignored) {
        }
        return unwrapped != null ? unwrapped : wrapper;
    }

}

package com.oneinstep.myspi0.core;

import lombok.extern.slf4j.Slf4j;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JDK 代码编译器
 */
@Slf4j
public class JdkCodeCompiler {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([$_a-zA-Z][$_a-zA-Z0-9.]*);");
    private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+");
    public static final String CLASS_EXTENSION = ".class";

    public static final String JAVA_EXTENSION = ".java";
    // 类创建中的锁
    protected static final ConcurrentHashMap<String, Lock> CLASS_IN_CREATION_MAP = new ConcurrentHashMap<>();

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
                return doCompile(sourceCode, className);
            } catch (Exception t) {
                throw new IllegalStateException("Failed to compile class, cause: " + t.getMessage() + ", class: "
                        + className + ", code: \n" + sourceCode + "\n, stack: " + exceptiontoString(t));
            }
        } finally {
            // 释放锁
            lock.unlock();
        }
    }

    private static String getClassName(String code) {
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

    // 编译器
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    // 诊断收集器
    private final DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
    // 源码文件管理器
    private final JavaFileManagerImpl javaFileManager;
    // 编译选项
    private final List<String> options;
    // 默认 Java 版本
    private static final String DEFAULT_JAVA_VERSION = "17";

    public JdkCodeCompiler() {
        this(buildDefaultOptions());
    }

    private static List<String> buildDefaultOptions(String javaVersion) {
        return Arrays.asList("-source", javaVersion, "-target", javaVersion);
    }

    private static List<String> buildDefaultOptions() {
        return buildDefaultOptions(DEFAULT_JAVA_VERSION);
    }

    public JdkCodeCompiler(List<String> options) {
        this.options = new ArrayList<>(options);
        StandardJavaFileManager manager = compiler.getStandardFileManager(diagnosticCollector, null, null);
        final ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();
        if (parentClassLoader instanceof URLClassLoader urlClassLoader && !isAppClassLoader(parentClassLoader)) {
            try {
                List<File> files = new ArrayList<>();
                for (URL url : urlClassLoader.getURLs()) {
                    files.add(new File(url.getFile()));
                }
                manager.setLocation(StandardLocation.CLASS_PATH, files);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        // 自定义类加载器
        ClassLoaderImpl classLoader = new ClassLoaderImpl(parentClassLoader);
        javaFileManager = new JavaFileManagerImpl(manager, classLoader);
    }

    protected Class<?> doCompile(String sourceCode, String className) throws Exception {
        String name = getClassName(sourceCode);
        int i = name.lastIndexOf('.');
        // 包名
        String packageName = i < 0 ? "" : name.substring(0, i);

        // 创建 Java 源码文件对象
        JavaFileObjectImpl javaFileObject = new JavaFileObjectImpl(className, sourceCode);
        // 将源码文件对象放入文件管理器
        javaFileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName, className + JAVA_EXTENSION, javaFileObject);
        // 启动编译任务
        Boolean result = compiler.getTask(
                null,
                javaFileManager,
                diagnosticCollector,
                options,
                null,
                Collections.singletonList(javaFileObject)).call();
        if (result == null || !result) {
            // 编译失败
            throw new IllegalStateException(
                    "Compilation failed. class: " + name + ", diagnostics: " + diagnosticCollector.getDiagnostics());
        }
        // 加载类
        return javaFileManager.getClassLoader(null).loadClass(name);
    }

    private static boolean isAppClassLoader(ClassLoader classLoader) {
        try {
            Class<?> launcherClass = Class.forName("jdk.internal.loader.ClassLoaders$AppClassLoader");
            return launcherClass.isInstance(classLoader);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Java 源码文件对象
     */
    private static final class JavaFileObjectImpl extends SimpleJavaFileObject {

        private final CharSequence source;
        private ByteArrayOutputStream bytecode;

        public JavaFileObjectImpl(final String baseName, final CharSequence source) {
            super(Objects.requireNonNull(toURI(baseName + JAVA_EXTENSION)), Kind.SOURCE);
            this.source = source;
        }

        JavaFileObjectImpl(final String name, final Kind kind) {
            super(Objects.requireNonNull(toURI(name)), kind);
            source = null;
        }

        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws UnsupportedOperationException {
            if (source == null) {
                throw new UnsupportedOperationException("source == null");
            }
            return source;
        }

        @Override
        public InputStream openInputStream() {
            return new ByteArrayInputStream(getByteCode());
        }

        @Override
        public OutputStream openOutputStream() {
            bytecode = new ByteArrayOutputStream();
            return bytecode;
        }

        public byte[] getByteCode() {
            return bytecode.toByteArray();
        }
    }

    /**
     * Java 文件管理器
     */
    private static final class JavaFileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {

        // 类加载器
        private final ClassLoaderImpl classLoader;
        // 源码文件对象
        private final Map<URI, JavaFileObject> fileObjects = new HashMap<>();

        public JavaFileManagerImpl(JavaFileManager fileManager, ClassLoaderImpl classLoader) {
            super(fileManager);
            this.classLoader = classLoader;
        }

        @Override
        public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
            FileObject o = fileObjects.get(uri(location, packageName, relativeName));
            if (o != null) {
                return o;
            }
            return super.getFileForInput(location, packageName, relativeName);
        }

        /**
         * 将源码文件对象放入文件管理器
         *
         * @param location     位置
         * @param packageName  包名
         * @param relativeName 相对名
         * @param file         源码文件
         */
        public void putFileForInput(StandardLocation location, String packageName, String relativeName, JavaFileObject file) {
            fileObjects.put(uri(location, packageName, relativeName), file);
        }

        private URI uri(Location location, String packageName, String relativeName) {
            return toURI(location.getName() + '/' + packageName + '/' + relativeName);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, JavaFileObject.Kind kind, FileObject outputFile) {
            JavaFileObjectImpl file = new JavaFileObjectImpl(qualifiedName, kind);
            // 添加编译后的类文件到类加载器
            classLoader.add(qualifiedName, file);
            return file;
        }

        @Override
        public ClassLoader getClassLoader(Location location) {
            return classLoader;
        }

        @Override
        public String inferBinaryName(Location loc, JavaFileObject file) {
            if (file instanceof JavaFileObjectImpl) {
                return file.getName();
            }
            return super.inferBinaryName(loc, file);
        }

        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse)
                throws IOException {
            Iterable<JavaFileObject> result = super.list(location, packageName, kinds, recurse);

            ArrayList<JavaFileObject> files = new ArrayList<>();

            // 从 CLASS_PATH 中获取编译后的类文件
            if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
                for (JavaFileObject file : fileObjects.values()) {
                    if (file.getKind() == JavaFileObject.Kind.CLASS && file.getName().startsWith(packageName)) {
                        files.add(file);
                    }
                }
                // 添加类加载器中的类文件
                files.addAll(classLoader.files());
            }
            // 如果是源码文件 则从 SOURCE_PATH 中获取源码文件
            else if (location == StandardLocation.SOURCE_PATH && kinds.contains(JavaFileObject.Kind.SOURCE)) {
                for (JavaFileObject file : fileObjects.values()) {
                    if (file.getKind() == JavaFileObject.Kind.SOURCE && file.getName().startsWith(packageName)) {
                        files.add(file);
                    }
                }
            }

            for (JavaFileObject file : result) {
                files.add(file);
            }

            return files;
        }
    }

    /**
     * 自定义类加载器
     */
    private static final class ClassLoaderImpl extends ClassLoader {

        // 获取编译后的类文件
        private final Map<String, JavaFileObject> classes = new HashMap<>();

        ClassLoaderImpl(final ClassLoader parentClassLoader) {
            super(parentClassLoader);
        }

        Collection<JavaFileObject> files() {
            return Collections.unmodifiableCollection(classes.values());
        }

        /**
         * 重写 findClass 方法
         *
         * @param qualifiedClassName 类名
         * @return 类
         * @throws ClassNotFoundException 类未找到异常
         */
        @Override
        protected Class<?> findClass(final String qualifiedClassName) throws ClassNotFoundException {
            // 获取编译后的类文件
            JavaFileObject file = classes.get(qualifiedClassName);
            if (file != null) {
                // 获取类字节码
                byte[] bytes = ((JavaFileObjectImpl) file).getByteCode();
                // 使用字节码定义类
                return defineClass(qualifiedClassName, bytes, 0, bytes.length);
            }
            return super.findClass(qualifiedClassName);
        }

        /**
         * 添加编译后的类文件到类加载器
         *
         * @param qualifiedClassName 类名
         * @param javaFile           类文件
         */
        void add(final String qualifiedClassName, final JavaFileObject javaFile) {
            classes.put(qualifiedClassName, javaFile);
        }

        @Override
        public InputStream getResourceAsStream(final String name) {
            // 获取类文件输入流
            if (name.endsWith(CLASS_EXTENSION)) {
                String qualifiedClassName = name.substring(0, name.length() - CLASS_EXTENSION.length()).replace('/', '.');
                JavaFileObjectImpl file = (JavaFileObjectImpl) classes.get(qualifiedClassName);
                if (file != null) {
                    return new ByteArrayInputStream(file.getByteCode());
                }
            }
            // 获取资源输入流
            return super.getResourceAsStream(name);
        }
    }

    public static String exceptiontoString(Throwable e) {
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

    public static URI toURI(String name) {
        try {
            return new URI(name);
        } catch (URISyntaxException e) {
            log.error("toURI error", e);
        }
        return null;
    }

}
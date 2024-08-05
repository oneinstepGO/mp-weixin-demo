package com.oneinstep.myspi.core.compile;

import com.oneinstep.myspi.core.utils.ClassUtils;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * JDK 代码编译器
 */
public class JdkCodeCompiler extends AbsCodeCompiler {

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

    public JdkCodeCompiler(String javaVersion) {
        this(buildDefaultOptions(javaVersion));
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
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader instanceof URLClassLoader urlClassLoader && !isAppClassLoader(loader)) {
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
        ClassLoaderImpl classLoader = new ClassLoaderImpl(loader);
        javaFileManager = new JavaFileManagerImpl(manager, classLoader);
    }

    @Override
    protected Class<?> doCompile(String sourceCode, String className, ClassLoader classLoader) throws Exception {
        String name = getClassName(sourceCode);
        int i = name.lastIndexOf('.');
        // 包名
        String packageName = i < 0 ? "" : name.substring(0, i);

        // 创建 Java 源码文件对象
        JavaFileObjectImpl javaFileObject = new JavaFileObjectImpl(className, sourceCode);
        // 将源码文件对象放入文件管理器
        javaFileManager.putFileForInput(StandardLocation.SOURCE_PATH, packageName, className + ClassUtils.JAVA_EXTENSION, javaFileObject);
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
            super(Objects.requireNonNull(ClassUtils.toURI(baseName + ClassUtils.JAVA_EXTENSION)), Kind.SOURCE);
            this.source = source;
        }

        JavaFileObjectImpl(final String name, final Kind kind) {
            super(Objects.requireNonNull(ClassUtils.toURI(name)), kind);
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
            return ClassUtils.toURI(location.getName() + '/' + packageName + '/' + relativeName);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String qualifiedName, JavaFileObject.Kind kind, FileObject outputFile) {
            JavaFileObjectImpl file = new JavaFileObjectImpl(qualifiedName, kind);
            // 添加编译后的类文件到类加载器
            classLoader.add(qualifiedName, file);
            return file;
        }

        @Override
        public ClassLoader getClassLoader(JavaFileManager.Location location) {
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
        protected synchronized Class<?> loadClass(final String name, final boolean resolve)
                throws ClassNotFoundException {
            return super.loadClass(name, resolve);
        }

        @Override
        public InputStream getResourceAsStream(final String name) {
            if (name.endsWith(ClassUtils.CLASS_EXTENSION)) {
                String qualifiedClassName = name.substring(0, name.length() - ClassUtils.CLASS_EXTENSION.length())
                        .replace('/', '.');
                JavaFileObjectImpl file = (JavaFileObjectImpl) classes.get(qualifiedClassName);
                if (file != null) {
                    return new ByteArrayInputStream(file.getByteCode());
                }
            }
            return super.getResourceAsStream(name);
        }
    }
}
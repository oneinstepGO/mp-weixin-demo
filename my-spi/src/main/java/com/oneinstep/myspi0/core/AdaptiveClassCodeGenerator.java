package com.oneinstep.myspi0.core;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Adaptive class 代码生成器
 */
@Slf4j
public class AdaptiveClassCodeGenerator {

    private static final String CODE_PACKAGE = "package %s;\n\n";

    private static final String CODE_IMPORTS = "import %s;\n\n";

    private static final String CODE_CLASS_DECLARATION = "public class %s$Adaptive implements %s {\n\n";

    private static final String CODE_METHOD_DECLARATION = "    public %s %s(%s) %s {\n%s    }\n\n";

    private static final String CODE_METHOD_ARGUMENT = "%s arg%d";

    private static final String CODE_METHOD_THROWS = "throws %s";

    private static final String CODE_URL_NULL_CHECK =
            "        if (arg%d == null) throw new IllegalArgumentException(\"url == null\");\n        %s url = arg%d;\n";

    private static final String CODE_EXT_NAME_ASSIGNMENT = "        String extName = %s;\n";

    private static final String CODE_EXT_NAME_NULL_CHECK = "        if(extName == null) "
            + "throw new IllegalStateException(\"Failed to get extension (%s) name from url (\" + url + \") use keys(%s)\");\n";

    private static final String CODE_EXTENSION_ASSIGNMENT =
            "        %s extension = ExtensionLoader.getExtensionLoader(%s.class).getExtension(extName);\n";

    private static final String CODE_EXTENSION_METHOD_INVOKE_ARGUMENT = "arg%d";

    private static final String PROTOCOL_KEY = "protocol";

    private final Class<?> type;


    public AdaptiveClassCodeGenerator(Class<?> type) {
        this.type = type;
    }

    /**
     * test if given type has at least one method annotated with <code>Adaptive</code>
     */
    private boolean hasAdaptiveMethod() {
        return Arrays.stream(type.getMethods()).anyMatch(m -> m.isAnnotationPresent(Adaptive.class));
    }

    /**
     * generate and return class code
     */
    public String generate() {
        // no need to generate adaptive class since there's no adaptive method found.
        if (!hasAdaptiveMethod()) {
            throw new IllegalStateException("No adaptive method exist on extension " + type.getName()
                    + ", refuse to create the adaptive class!");
        }

        StringBuilder code = new StringBuilder();
        code.append(generatePackageInfo());
        code.append(generateImports());
        code.append(generateClassDeclaration());

        Method[] methods = type.getMethods();

        for (Method method : methods) {
            code.append(generateMethod(method));
        }
        code.append('}');

        String finalCode = code.toString();
        System.out.println("=========================  生成代码 =========================");
        System.out.println(finalCode);
        System.out.println("=========================  生成代码 =========================");
        return finalCode;
    }

    /**
     * generate package info
     */
    private String generatePackageInfo() {
        return String.format(CODE_PACKAGE, type.getPackage().getName());
    }

    /**
     * generate imports
     */
    private String generateImports() {
        return String.format(CODE_IMPORTS, ExtensionLoader.class.getName());
    }

    /**
     * generate class declaration
     */
    private String generateClassDeclaration() {
        return String.format(CODE_CLASS_DECLARATION, type.getSimpleName(), type.getCanonicalName());
    }

    /**
     * get index of parameter with type URL
     */
    private int getUrlTypeIndex(Method method) {
        int urlTypeIndex = -1;
        Class<?>[] pts = method.getParameterTypes();
        for (int i = 0; i < pts.length; ++i) {
            if (pts[i].equals(URL.class)) {
                urlTypeIndex = i;
                break;
            }
        }
        return urlTypeIndex;
    }

    /**
     * generate method declaration
     */
    private String generateMethod(Method method) {
        String methodReturnType = method.getReturnType().getCanonicalName();
        String methodName = method.getName();
        String methodContent = generateMethodContent(method);
        String methodArgs = generateMethodArguments(method);
        String methodThrows = generateMethodThrows(method);
        return String.format(CODE_METHOD_DECLARATION, methodReturnType, methodName, methodArgs, methodThrows, methodContent);
    }

    /**
     * generate method arguments
     */
    private String generateMethodArguments(Method method) {
        Class<?>[] pts = method.getParameterTypes();
        return IntStream.range(0, pts.length)
                .mapToObj(i -> String.format(CODE_METHOD_ARGUMENT, pts[i].getCanonicalName(), i))
                .collect(Collectors.joining(", "));
    }

    /**
     * generate method throws
     */
    private String generateMethodThrows(Method method) {
        Class<?>[] ets = method.getExceptionTypes();
        if (ets.length > 0) {
            String list = Arrays.stream(ets).map(Class::getCanonicalName).collect(Collectors.joining(", "));
            return String.format(CODE_METHOD_THROWS, list);
        } else {
            return "";
        }
    }

    /**
     * generate method URL argument null check
     */
    private String generateUrlNullCheck(int index) {
        return String.format(CODE_URL_NULL_CHECK, index, URL.class.getName(), index);
    }

    /**
     * generate method content
     */
    private String generateMethodContent(Method method) {
        Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
        StringBuilder code = new StringBuilder(512);

        int urlTypeIndex = getUrlTypeIndex(method);

        // found parameter in URL type
        if (urlTypeIndex != -1) {
            // Null Point check
            code.append(generateUrlNullCheck(urlTypeIndex));
        } else {
            // did not find parameter in URL type
            throw new IllegalStateException("The method " + method.getName() + " must has parameter of type URL");
        }

        String value = adaptiveAnnotation.value();

        code.append(generateExtNameAssignment(value));

        // check extName == null?
        code.append(generateExtNameNullCheck(value));

        // extension assignment
        code.append(generateExtensionAssignment());

        // return statement
        code.append(generateReturnAndInvocation(method));


        return code.toString();
    }

    private String generateExtensionAssignment() {
        return String.format(CODE_EXTENSION_ASSIGNMENT, type.getName(), type.getName());
    }

    /**
     * generate code for variable extName null check
     */
    private String generateExtNameNullCheck(String value) {
        return String.format(CODE_EXT_NAME_NULL_CHECK, type.getName(), value);
    }

    /**
     * generate extName assignment code
     */
    private String generateExtNameAssignment(String value) {
        String getNameCode;
        if (!PROTOCOL_KEY.equals(value)) {
            getNameCode = String.format("url.getParameter(\"%s\")", value);
        } else {
            getNameCode = "url.getProtocol()";
        }
        return String.format(CODE_EXT_NAME_ASSIGNMENT, getNameCode);
    }

    /**
     * generate method invocation statement and return it if necessary
     */
    private String generateReturnAndInvocation(Method method) {
        String returnStatement = method.getReturnType().equals(void.class) ? "" : "        return ";

        String args = IntStream.range(0, method.getParameters().length)
                .mapToObj(i -> String.format(CODE_EXTENSION_METHOD_INVOKE_ARGUMENT, i))
                .collect(Collectors.joining(", "));

        return returnStatement + String.format("extension.%s(%s);%n", method.getName(), args);
    }

}

package com.oneinstep.myspi.core.compile;

import com.oneinstep.myspi.core.Adaptive;
import com.oneinstep.myspi.core.ExtensionLoader;
import com.oneinstep.myspi.core.URL;
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

    private static final String CLASS_NAME_INVOCATION = "org.apache.dubbo.rpc.Invocation";

    private static final String CODE_PACKAGE = "package %s;\n\n";

    private static final String CODE_IMPORTS = "import %s;\n\n";

    private static final String CODE_CLASS_DECLARATION = "public class %s$Adaptive implements %s {\n\n";

    private static final String CODE_METHOD_DECLARATION = "    public %s %s(%s) %s {\n%s    }\n\n";

    private static final String CODE_METHOD_ARGUMENT = "%s arg%d";

    private static final String CODE_METHOD_THROWS = "throws %s";

    private static final String CODE_UNSUPPORTED =
            "        throw new UnsupportedOperationException(\"The method %s of interface %s is not adaptive method!\");\n";

    private static final String CODE_URL_NULL_CHECK =
            "        if (arg%d == null) throw new IllegalArgumentException(\"url == null\");\n        %s url = arg%d;\n";

    private static final String CODE_EXT_NAME_ASSIGNMENT = "        String extName = %s;\n";

    private static final String CODE_EXT_NAME_NULL_CHECK = "        if(extName == null) "
            + "throw new IllegalStateException(\"Failed to get extension (%s) name from url (\" + url + \") use keys(%s)\");\n";

    private static final String CODE_INVOCATION_ARGUMENT_NULL_CHECK =
            "        if (arg%d == null) throw new IllegalArgumentException(\"invocation == null\"); "
                    + "String methodName = arg%d.getMethodName();\n";

    private static final String CODE_EXTENSION_ASSIGNMENT =
            "        %s extension = ExtensionLoader.getExtensionLoader(%s.class).getExtension(extName);\n";

    private static final String CODE_GET_METHOD_PARAMETER = "url.getMethodParameter(methodName, \"%s\", \"%s\")";

    private static final String CODE_EXTENSION_METHOD_INVOKE_ARGUMENT = "arg%d";

    private static final String PROTOCOL_KEY = "protocol";

    private final Class<?> type;

    private final String defaultExtName;

    public AdaptiveClassCodeGenerator(Class<?> type, String defaultExtName) {
        this.type = type;
        this.defaultExtName = defaultExtName;
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
        System.out.println("========================= generate adaptive class code start =========================");
        System.out.println(code);
        System.out.println("========================= generate adaptive class code end =========================");
        return code.toString();
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
     * generate method not annotated with Adaptive with throwing unsupported exception
     */
    private String generateUnsupported(Method method) {
        return String.format(CODE_UNSUPPORTED, method, type.getName());
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
        return String.format(
                CODE_METHOD_DECLARATION, methodReturnType, methodName, methodArgs, methodThrows, methodContent);
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
        if (adaptiveAnnotation == null) {
            return generateUnsupported(method);
        } else {
            int urlTypeIndex = getUrlTypeIndex(method);

            // found parameter in URL type
            if (urlTypeIndex != -1) {
                // Null Point check
                code.append(generateUrlNullCheck(urlTypeIndex));
            } else {
                // did not find parameter in URL type
                throw new IllegalStateException("The method " + method.getName() + " must has parameter of type URL");
            }

            String[] value = adaptiveAnnotation.value();

            boolean hasInvocation = hasInvocationArgument(method);

            code.append(generateInvocationArgumentNullCheck(method));

            code.append(generateExtNameAssignment(value, hasInvocation));

            // check extName == null?
            code.append(generateExtNameNullCheck(value));

            // extension assignment
            code.append(generateExtensionAssignment());

            // return statement
            code.append(generateReturnAndInvocation(method));
        }

        return code.toString();
    }

    private String generateExtensionAssignment() {
        return String.format(CODE_EXTENSION_ASSIGNMENT, type.getName(), type.getName());
    }

    /**
     * generate code for variable extName null check
     */
    private String generateExtNameNullCheck(String[] value) {
        return String.format(CODE_EXT_NAME_NULL_CHECK, type.getName(), Arrays.toString(value));
    }

    /**
     * generate extName assignment code
     */
    private String generateExtNameAssignment(String[] value, boolean hasInvocation) {
        String getNameCode = null;
        for (int i = value.length - 1; i >= 0; --i) {
            if (i == value.length - 1) {
                if (null != defaultExtName) {
                    if (!PROTOCOL_KEY.equals(value[i])) {
                        if (hasInvocation) {
                            getNameCode = String.format(CODE_GET_METHOD_PARAMETER, value[i], defaultExtName);
                        } else {
                            getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], defaultExtName);
                        }
                    } else {
                        getNameCode = String.format(
                                "( url.getProtocol() == null ? \"%s\" : url.getProtocol() )", defaultExtName);
                    }
                } else {
                    if (!PROTOCOL_KEY.equals(value[i])) {
                        if (hasInvocation) {
                            getNameCode = String.format(
                                    CODE_GET_METHOD_PARAMETER, value[i], null);
                        } else {
                            getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                        }
                    } else {
                        getNameCode = "url.getProtocol()";
                    }
                }
            } else {
                if (!PROTOCOL_KEY.equals(value[i])) {
                    if (hasInvocation) {
                        getNameCode = String.format(
                                "url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                    } else {
                        getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i], getNameCode);
                    }
                } else {
                    getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()", getNameCode);
                }
            }
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

    /**
     * test if method has argument of type <code>Invocation</code>
     */
    private boolean hasInvocationArgument(Method method) {
        Class<?>[] pts = method.getParameterTypes();
        return Arrays.stream(pts).anyMatch(p -> CLASS_NAME_INVOCATION.equals(p.getName()));
    }

    /**
     * generate code to test argument of type <code>Invocation</code> is null
     */
    private String generateInvocationArgumentNullCheck(Method method) {
        Class<?>[] pts = method.getParameterTypes();
        return IntStream.range(0, pts.length)
                .filter(i -> CLASS_NAME_INVOCATION.equals(pts[i].getName()))
                .mapToObj(i -> String.format(CODE_INVOCATION_ARGUMENT_NULL_CHECK, i, i))
                .findFirst()
                .orElse("");
    }

}

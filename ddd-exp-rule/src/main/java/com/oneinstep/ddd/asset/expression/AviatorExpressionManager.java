package com.oneinstep.ddd.asset.expression;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Feature;
import com.googlecode.aviator.Options;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 表达式引擎管理器
 */
@Slf4j
public class AviatorExpressionManager {

    private AviatorExpressionManager() {
    }

    static {
        // 设置超时时间
        AviatorEvaluator.setOption(Options.EVAL_TIMEOUT_MS, 300);
        // 设置最大循环次数
        AviatorEvaluator.setOption(Options.MAX_LOOP_COUNT, 1000);
        // 关闭模块系统
        AviatorEvaluator.getInstance().disableFeature(Feature.Module);
        // 设置函数缺失处理
        AviatorEvaluator.getInstance().setFunctionMissing(null);

        // 设置允许的类
        final HashSet<Object> classes = new HashSet<>();
        AviatorEvaluator.setOption(Options.ALLOWED_CLASS_SET, classes);
    }

    /**
     * 表达式缓存
     */
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        private static final AviatorExpressionManager INSTANCE = new AviatorExpressionManager();
    }

    public static AviatorExpressionManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 注册并编译表达式
     *
     * @param formulaName      公式名称
     * @param expressionScript 表达式脚本
     */
    public void registerExpression(String formulaName, String expressionScript) {
        if (StringUtils.isBlank(expressionScript)) {
            // load from file
            registerExpressionFromFile(formulaName);
            return;
        }

        // 开启缓存
        Expression compiledExpression = AviatorEvaluator.compile(expressionScript, true);
        Expression expression = expressionCache.putIfAbsent(formulaName, compiledExpression);
        if (expression != null) {
            log.warn("Expression already exists: {}", formulaName);
            throw new IllegalArgumentException("Expression already exists: " + formulaName);
        }
    }

    /**
     * 从文件中注册表达式
     *
     * @param formulaName 公式名称
     */
    public void registerExpressionFromFile(String formulaName) {
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream("formula/" + formulaName + ".av");
        if (inputStream == null) {
            throw new IllegalArgumentException("Expression file not found: " + formulaName);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            // read all lines
            String expression = reader.lines().collect(Collectors.joining("\n"));
            registerExpression(formulaName, expression);
        } catch (IOException e) {
            log.error("读取公式文件失败", e);
        }
    }

    /**
     * 执行表达式计算
     *
     * @param formulaName 公式名称
     * @param params      计算参数
     * @return 计算结果
     */
    public Object executeExpression(String formulaName, Map<String, Object> params) {
        Expression expression = expressionCache.get(formulaName);
        if (expression == null) {
            throw new IllegalArgumentException("Expression not found: " + formulaName);
        }
        return expression.execute(params);
    }

    /**
     * 移除表达式
     *
     * @param formulaName 表达式标识
     */
    public void removeExpression(String formulaName) {
        expressionCache.remove(formulaName);
    }

    /**
     * 获取表达式
     *
     * @param formulaName 表达式标识
     * @return 表达式
     */
    public Expression getExpression(String formulaName) {
        return expressionCache.get(formulaName);
    }

}
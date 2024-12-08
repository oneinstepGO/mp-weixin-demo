package com.oneinstep.ddd.asset;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * BigDecimal 表达式测试
 */
class BigDecimalExpressionTest {

    @Test
    void test() {
        // 创建 BigDecimal 类型的数据
        BigDecimal tnBalance = new BigDecimal("200.00"); // Tn日结余
        BigDecimal nav = new BigDecimal("400.00"); // 净值
        BigDecimal totalInitialMargin = new BigDecimal("300.00");
        BigDecimal totalMaintenanceMargin = new BigDecimal("200.00");
        BigDecimal riskCoefficient = new BigDecimal("0.1");

        // 准备表达式
        String expression = "if 帐户类型 == 'Cash' {" +
                "   return max(Tn日结余, 0);" +
                "} elsif 帐户类型 == 'Margin' {" +
                "   return max(min(Tn日结余, 资产净值 - 风控系数 * (总初始保证金 - 总维持保证金) - 总维持保证金), 0);" +
                "} else {" +
                "   throw new Exception(\"账户类型错误\");" +
                "}";

        System.out.println(expression);

        // 编译表达式
        Expression compiledExp = AviatorEvaluator.compile(expression);

        // 设置变量
        Map<String, Object> env = new HashMap<>();
        env.put("Tn日结余", tnBalance);
        env.put("资产净值", nav);
        env.put("总初始保证金", totalInitialMargin);
        env.put("总维持保证金", totalMaintenanceMargin);
        env.put("风控系数", riskCoefficient);
        env.put("帐户类型", "Margin");

        // 执行表达式
        BigDecimal result = (BigDecimal) compiledExp.execute(env);
        System.out.println("计算结果: " + result);

        Assertions.assertEquals(0, new BigDecimal("190.00").compareTo(result));
    }

}

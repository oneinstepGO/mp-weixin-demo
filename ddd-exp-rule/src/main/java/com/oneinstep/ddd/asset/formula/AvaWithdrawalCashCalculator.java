package com.oneinstep.ddd.asset.formula;

import com.oneinstep.ddd.asset.aggregate.MoneyAccount;
import com.oneinstep.ddd.asset.enums.VerifyTypeConstants;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 现金可取 金额计算器
 */
@Component
@Rule
@Slf4j
public class AvaWithdrawalCashCalculator extends AbsCalculator {

    public AvaWithdrawalCashCalculator() {
        super("availableWithdrawalCash");
    }

    @Override
    protected Map<String, Object> initEnvironment(MoneyAccount moneyAccount) {
        // 创建 BigDecimal 类型的数据 get data from api , but mock here
        BigDecimal tnBalance = new BigDecimal("200.00"); // Tn日结余
        BigDecimal nav = new BigDecimal("400.00"); // 净值
        BigDecimal totalInitialMargin = new BigDecimal("300.00");
        BigDecimal totalMaintenanceMargin = new BigDecimal("200.00");
        BigDecimal riskCoefficient = new BigDecimal("0.1");

        // 设置变量
        Map<String, Object> env = new HashMap<>();
        env.put("Tn日结余", tnBalance);
        env.put("资产净值", nav);
        env.put("总初始保证金", totalInitialMargin);
        env.put("总维持保证金", totalMaintenanceMargin);
        env.put("风控系数", riskCoefficient);
        env.put("帐户类型", moneyAccount.getAccountType() == 1 ? "Cash" : "Margin");
        return env;
    }

    @Condition
    public boolean shouldUseThisRule(@Fact("verifyType") Integer verifyType,
                                     @Fact("unitedCredit") boolean unitedCredit) {
        return verifyType == VerifyTypeConstants.VERIFY_AVAILABLE_WITHDRAWAL_BALANCE && !unitedCredit;
    }

}

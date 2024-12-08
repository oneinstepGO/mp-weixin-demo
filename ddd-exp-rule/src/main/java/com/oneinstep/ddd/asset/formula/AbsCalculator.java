package com.oneinstep.ddd.asset.formula;

import com.googlecode.aviator.Expression;
import com.oneinstep.ddd.asset.aggregate.MoneyAccount;
import com.oneinstep.ddd.asset.expression.AviatorExpressionManager;
import com.oneinstep.ddd.asset.util.Holder;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Fact;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static com.oneinstep.ddd.asset.formula.CalRuleManager.*;

/**
 * 可取现金额计算器
 */
@Getter
@Slf4j
public abstract class AbsCalculator implements VerifyAmountCalculator {

    /**
     * 公式名称
     */
    protected String formulaName;

    protected AbsCalculator(String formulaName) {
        if (StringUtils.isBlank(formulaName)) {
            throw new IllegalArgumentException("公式名称不能为空");
        }
        this.formulaName = formulaName;
    }

    /**
     * 获取表达式脚本
     *
     * @return 表达式脚本
     */
    public String getExpressionScript() {
        return null;
    }

    /**
     * 初始化环境变量
     *
     * @param moneyAccount 资金账户
     * @return 环境变量
     */
    protected abstract Map<String, Object> initEnvironment(MoneyAccount moneyAccount);

    /**
     * 注册表达式
     */
    @PostConstruct
    public void registerExpression() {
        AviatorExpressionManager.getInstance().registerExpression(this.getFormulaName(), this.getExpressionScript());
    }

    /**
     * 计算
     *
     * @param moneyAccount 资金账户
     * @param verifyAmount 验证金额
     * @param resultHolder 结果持有者
     */
    @Action
    public void doVerify(@Fact(MONEY_ACCOUNT) MoneyAccount moneyAccount,
                         @Fact(VERIFY_AMOUNT) BigDecimal verifyAmount,
                         @Fact(RESULT_HOLDER) Holder<Boolean> resultHolder) {
        BigDecimal calAmount = calculate(moneyAccount);
        log.info("moneyAccount: {}, verifyAmount: {}, calAmount: {}", moneyAccount, verifyAmount, calAmount);
        resultHolder.set(calAmount.compareTo(verifyAmount) >= 0);
    }

    /**
     * 计算
     *
     * @param moneyAccount 资金账户
     * @return 计算结果
     */
    public BigDecimal calculate(MoneyAccount moneyAccount) {
        Expression expression = AviatorExpressionManager.getInstance().getExpression(this.formulaName);
        Map<String, Object> environment = initEnvironment(moneyAccount);
        Object result = expression.execute(environment);
        if (result instanceof BigDecimal amount) {
            // 返回两位小数
            return amount.setScale(2, RoundingMode.HALF_UP);
        }
        log.error("计算结果必须是BigDecimal类型, result: {}", result);
        throw new IllegalArgumentException("计算结果必须是BigDecimal类型");
    }

}

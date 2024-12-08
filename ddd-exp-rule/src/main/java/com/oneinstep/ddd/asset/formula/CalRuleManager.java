package com.oneinstep.ddd.asset.formula;

import com.oneinstep.ddd.asset.aggregate.MoneyAccount;
import com.oneinstep.ddd.asset.util.Holder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.api.RulesEngineParameters;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class CalRuleManager {

    private final List<AbsCalculator> calculators;

    private volatile RulesEngine verifyEngine;

    private volatile Rules rules;

    public static final String RESULT_HOLDER = "RESULT_HOLDER";

    public static final String VERIFY_MONEY_TYPE = "verifyMoneyType";

    public static final String VERIFY_TYPE = "verifyType";

    public static final String UNITED_CREDIT = "unitedCredit";

    public static final String VERIFY_AMOUNT = "verifyAmount";

    public static final String MONEY_ACCOUNT = "moneyAccount";

    @Autowired
    public CalRuleManager(List<AbsCalculator> calculators) {
        this.calculators = calculators;
    }

    @PostConstruct
    public void initRules() {
        // create rules engine
        RulesEngineParameters parameters = new RulesEngineParameters().skipOnFirstAppliedRule(true);
        this.verifyEngine = new DefaultRulesEngine(parameters);

        // register rules
        this.rules = new Rules();
        calculators.forEach(calculator -> this.rules.register(calculator));

        // register no verify rule
        this.rules.register(new NoVerifyRule());
    }

    /**
     * 校验规则
     *
     * @param <T>   返回值类型
     * @param facts 参数
     * @return 返回值
     */
    public <T> T verify(Facts facts) {
        Holder<T> resultHolder = new Holder<>();
        facts.put(RESULT_HOLDER, resultHolder);
        verifyEngine.fire(rules, facts);
        return resultHolder.get();
    }

    /**
     * 校验规则
     *
     * @param verifyType   校验类型
     * @param unitedCredit 是否统一授信
     * @param verifyAmount 校验金额
     * @param moneyAccount 资金账户
     * @return 校验结果
     */
    public boolean verify(int verifyMoneyType, int verifyType, boolean unitedCredit, BigDecimal verifyAmount,
                          MoneyAccount moneyAccount) {
        Facts facts = new Facts();
        facts.put(VERIFY_MONEY_TYPE, verifyMoneyType);
        facts.put(VERIFY_TYPE, verifyType);
        facts.put(UNITED_CREDIT, unitedCredit);
        facts.put(VERIFY_AMOUNT, verifyAmount);
        facts.put(MONEY_ACCOUNT, moneyAccount);
        return verify(facts);
    }

}

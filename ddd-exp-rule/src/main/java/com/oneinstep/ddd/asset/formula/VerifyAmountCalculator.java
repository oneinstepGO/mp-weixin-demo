package com.oneinstep.ddd.asset.formula;

import com.oneinstep.ddd.asset.aggregate.MoneyAccount;

import java.math.BigDecimal;

/**
 * 公式计算器
 */
public interface VerifyAmountCalculator {

    /**
     * 根据用户钱包计算各种金额的公式
     *
     * @param moneyAccount 用户钱包
     * @return 计算结果
     */
    BigDecimal calculate(MoneyAccount moneyAccount);
}

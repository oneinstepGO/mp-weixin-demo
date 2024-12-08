package com.oneinstep.ddd.asset.value;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 金额值对象
 * 
 * @param currency 币种
 * @param amount   金额
 */
public record MoneyAmount(Currency currency, BigDecimal amount) {

    /**
     * 加法
     * 
     * @param other 另一个金额
     * @return 新的金额
     */
    public MoneyAmount add(MoneyAmount other) {
        if (!Objects.equals(this.currency, other.currency)) {
            throw new IllegalArgumentException("Cannot add money of different types");
        }
        return new MoneyAmount(currency, amount.add(other.amount));
    }

    /**
     * 创建金额
     * 
     * @param currency 币种
     * @param amount   金额
     * @return 金额
     */
    public static MoneyAmount of(Currency currency, BigDecimal amount) {
        return new MoneyAmount(currency, amount);
    }
    
}

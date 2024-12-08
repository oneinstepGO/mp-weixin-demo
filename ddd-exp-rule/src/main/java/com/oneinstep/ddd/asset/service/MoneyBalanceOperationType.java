package com.oneinstep.ddd.asset.service;

import lombok.Getter;

@Getter
public enum MoneyBalanceOperationType {

    /**
     * 存入
     */
    DEPOSIT(1L),
    /**
     * 取出
     */
    WITHDRAW(1L << 1),
    /**
     * 冻结
     */
    FREEZE(1L << 2),
    /**
     * 解冻
     */
    UNFREEZE(1L << 3);

    /**
     * 获取值
     */
    private final long flagBit;

    MoneyBalanceOperationType(long flagBit) {
        this.flagBit = flagBit;
    }

}

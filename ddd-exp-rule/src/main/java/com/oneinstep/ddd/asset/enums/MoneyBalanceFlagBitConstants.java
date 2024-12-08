package com.oneinstep.ddd.asset.enums;

/**
 * 资金余额操作标志位常量
 */
public class MoneyBalanceFlagBitConstants {

    private MoneyBalanceFlagBitConstants() {
    }

    /**
     * 增加当前余额
     */
    public static final long INCREASE_CURRENT_BALANCE = 1L;

    /**
     * 减少当前余额
     */
    public static final long DECREASE_CURRENT_BALANCE = 1L << 1;

    /**
     * 增加冻结金额
     */
    public static final long INCREASE_FREEZE_AMOUNT = 1L << 2;

    /**
     * 减少冻结金额
     */
    public static final long DECREASE_FREEZE_AMOUNT = 1L << 3;

}

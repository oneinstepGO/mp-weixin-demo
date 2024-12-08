package com.oneinstep.ddd.asset.enums;

/**
 * 校验类型枚举
 */
public class VerifyTypeConstants {

    private VerifyTypeConstants() {
    }

    /**
     * 不校验
     */
    public static final int NO_VERIFY = -1;

    /**
     * 校验 可用余额
     */
    public static final int VERIFY_AVAILABLE_BALANCE = 1;

    /**
     * 校验 可取现余额
     */
    public static final int VERIFY_AVAILABLE_WITHDRAWAL_BALANCE = 2;

    /**
     * 校验 可冻结金额
     */
    public static final int VERIFY_FREEZE_AMOUNT = 3;

}

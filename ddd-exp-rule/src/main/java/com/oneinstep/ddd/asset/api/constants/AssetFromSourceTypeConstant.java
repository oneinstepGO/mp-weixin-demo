package com.oneinstep.ddd.asset.api.constants;

/**
 * 资产来源类型常量
 */
public class AssetFromSourceTypeConstant {

    private AssetFromSourceTypeConstant() {
    }

    /**
     * 现金充值
     */
    public static final Integer CASH_DEPOSIT = 500001;

    /**
     * 现金提现
     */
    public static final Integer CASH_WITHDRAW = 500002;

    /**
     * 买入冻结
     */
    public static final Integer BUY_FREEZE = 500003;

    /**
     * 订单取消解冻
     */
    public static final Integer ORDER_CANCEL_UNFREEZE = 500004;
}

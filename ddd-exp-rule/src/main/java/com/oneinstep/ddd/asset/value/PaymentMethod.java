package com.oneinstep.ddd.asset.value;

/**
 * 支付方式
 *
 * @param multipleCurrency 是否多币种支付
 * @param verifyType       验证公式
 *                         参考 {@link com.oneinstep.ddd.asset.enums.VerifyTypeConstants}
 * @param unitedCredit     是否统一授信
 */
public record PaymentMethod(boolean multipleCurrency, int verifyType, boolean unitedCredit) {

    public static PaymentMethod of(boolean multipleCurrency, int verifyType, boolean unitedCredit) {
        return new PaymentMethod(multipleCurrency, verifyType, unitedCredit);
    }

}

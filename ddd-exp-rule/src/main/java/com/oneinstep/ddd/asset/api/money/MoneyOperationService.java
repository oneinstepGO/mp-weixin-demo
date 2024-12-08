package com.oneinstep.ddd.asset.api.money;

import com.oneinstep.ddd.asset.value.FromSource;
import com.oneinstep.ddd.asset.value.MoneyAmount;
import com.oneinstep.ddd.asset.value.PaymentMethod;

import cn.hutool.core.lang.Pair;
import jakarta.annotation.Nonnull;

public interface MoneyOperationService {

    /**
     * 存入
     *
     * @param moneyAccountId 钱包ID
     * @param money          金额
     * @param fromSource     来源
     * @return Pair<Boolean, Long> 存入是否成功，操作记录ID
     */
    Pair<Boolean, Long> deposit(@Nonnull Long moneyAccountId, @Nonnull Integer accountType, @Nonnull MoneyAmount money,
                                @Nonnull FromSource fromSource);

    /**
     * 提现
     *
     * @param moneyAccountId 钱包ID
     * @param money          金额
     * @param fromSource     来源
     * @return Pair<Boolean, Long> 提现是否成功，操作记录ID
     */
    Pair<Boolean, Long> withdraw(@Nonnull Long moneyAccountId, @Nonnull Integer accountType, @Nonnull MoneyAmount money,
                                 @Nonnull FromSource fromSource, @Nonnull PaymentMethod paymentMethod);
}

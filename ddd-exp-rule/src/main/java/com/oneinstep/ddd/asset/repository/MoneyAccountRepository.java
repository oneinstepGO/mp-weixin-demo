package com.oneinstep.ddd.asset.repository;

import com.oneinstep.ddd.asset.aggregate.MoneyAccount;
import com.oneinstep.ddd.asset.domain.MoneyBalance;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 资金账户仓储
 */
@Repository
public class MoneyAccountRepository {

    @Resource
    private MoneyBalanceRepository moneyBalanceRepository;

    @Resource
    private ApplicationContext applicationContext;

    public MoneyAccount findById(Long moneyAccountId) {
        List<MoneyBalance> moneyBalances = moneyBalanceRepository.findByMoneyAccountId(moneyAccountId);
        if (CollectionUtils.isEmpty(moneyBalances)) {
            return null;
        }
        return new MoneyAccount(
                moneyAccountId,
                moneyBalances,
                applicationContext);
    }

}

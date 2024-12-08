package com.oneinstep.ddd.asset.repository;

import com.oneinstep.ddd.asset.domain.MoneyBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 资金余额仓储
 */
@Repository
public interface MoneyBalanceRepository extends JpaRepository<MoneyBalance, Long> {

    /**
     * 根据钱包ID查询钱包
     *
     * @param moneyAccountId 资金帐户id
     * @return 所有币种的钱包
     */
    List<MoneyBalance> findByMoneyAccountId(Long moneyAccountId);

}

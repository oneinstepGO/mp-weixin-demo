package com.oneinstep.ddd.asset.repository;

import com.oneinstep.ddd.asset.domain.MoneyBalanceOperation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 资金余额操作仓储
 */
public interface MoneyBalanceOperationRepository extends JpaRepository<MoneyBalanceOperation, Long> {

}

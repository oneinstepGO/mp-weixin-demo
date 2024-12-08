package com.oneinstep.ddd.asset.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneinstep.ddd.asset.domain.MoneyBalanceLog;

/**
 * 资金余额日志仓储
 */
public interface MoneyBalanceLogRepository extends JpaRepository<MoneyBalanceLog, Long> {

}

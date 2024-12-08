package com.oneinstep.ddd.asset.aggregate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.oneinstep.ddd.asset.domain.MoneyBalance;
import com.oneinstep.ddd.asset.domain.MoneyBalanceLog;
import com.oneinstep.ddd.asset.domain.MoneyBalanceOperation;
import com.oneinstep.ddd.asset.enums.MoneyBalanceOperationType;
import com.oneinstep.ddd.asset.event.MoneyBalanceChgEvent;
import com.oneinstep.ddd.asset.exception.MoneyNotEnoughException;
import com.oneinstep.ddd.asset.publisher.DomainEventPublisher;
import com.oneinstep.ddd.asset.repository.MoneyBalanceLogRepository;
import com.oneinstep.ddd.asset.repository.MoneyBalanceOperationRepository;
import com.oneinstep.ddd.asset.repository.MoneyBalanceRepository;
import com.oneinstep.ddd.asset.value.FromSource;
import com.oneinstep.ddd.asset.value.MoneyAmount;

import cn.hutool.core.lang.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 资金聚合根
 */
@Slf4j
@Getter
@ToString(exclude = { "moneyBalanceRepository", "moneyBalanceOperationRepository", "moneyBalanceLogRepository",
        "domainEventPublisher", "transactionTemplate" })
public class MoneyAccount {

    /**
     * 资金账户ID
     */
    @Getter
    private final Long moneyAccountId;

    /**
     * 账户类型
     */
    @Getter
    @Setter
    private int accountType = 1;

    /**
     * 钱包列表
     */
    private final List<MoneyBalance> moneyBalances;

    private final MoneyBalanceRepository moneyBalanceRepository;
    private final MoneyBalanceOperationRepository moneyBalanceOperationRepository;
    private final MoneyBalanceLogRepository moneyBalanceLogRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final TransactionTemplate transactionTemplate;

    public MoneyAccount(Long moneyAccountId,
            List<MoneyBalance> moneyBalances,
            ApplicationContext applicationContext) {
        this.moneyAccountId = moneyAccountId;
        this.moneyBalances = moneyBalances;
        this.moneyBalanceRepository = applicationContext.getBean(MoneyBalanceRepository.class);
        this.moneyBalanceOperationRepository = applicationContext.getBean(MoneyBalanceOperationRepository.class);
        this.moneyBalanceLogRepository = applicationContext.getBean(MoneyBalanceLogRepository.class);
        this.domainEventPublisher = applicationContext.getBean(DomainEventPublisher.class);
        this.transactionTemplate = applicationContext.getBean(TransactionTemplate.class);
    }

    /**
     * 存入资金
     *
     * @param money 存入金额
     * @return Pair<Boolean, Long> 存入是否成功，操作ID
     */
    public Pair<Boolean, Long> deposit(MoneyAmount money, FromSource fromSource) {
        MoneyBalance balance = findBalance(money.currency().code());
        if (balance == null) {
            log.error("钱包不存在，资金帐户ID：{}，币种类型：{}", moneyAccountId, money.currency().code());
            return new Pair<>(false, null);
        }

        return transactionTemplate.execute(status -> {
            try {
                // 记录操作前的余额
                MoneyBalance oldBalance = balance.copy();

                // 更新余额
                balance.deposit(money);

                // 持久化
                moneyBalanceRepository.save(balance);

                // 创建操作记录和日志
                MoneyBalanceOperation operation = new MoneyBalanceOperation();
                Pair<MoneyBalanceOperation, MoneyBalanceLog> depositPair = operation.deposit(balance, oldBalance,
                        money, fromSource);

                // 保存操作记录
                MoneyBalanceOperation savedOperation = moneyBalanceOperationRepository.save(depositPair.getKey());

                // 保存日志
                MoneyBalanceLog moneyBalanceLog = depositPair.getValue();
                moneyBalanceLog.setFromSourceId(savedOperation.getId());
                moneyBalanceLog.setFromSourceSubId(0L);
                MoneyBalanceLog savedLog = moneyBalanceLogRepository.save(moneyBalanceLog);

                publishCurrentBalanceChgEvent(MoneyBalanceOperationType.INCREASE.getOperationType(), money,
                        savedLog.getId(), fromSource);
                return new Pair<>(true, savedLog.getId());
            } catch (Exception e) {
                log.error("存入资金失败，资金帐户ID：{}，金额：{}", moneyAccountId, money, e);
                throw e;
            }
        });
    }

    /**
     * 取现
     *
     * @param money 取现金额
     * @return Pair<Boolean, Long> 取现是否成功，操作ID
     */
    public Pair<Boolean, Long> withdraw(MoneyAmount money, FromSource fromSource) {
        MoneyBalance balance = findBalance(money.currency().code());
        if (balance == null) {
            log.error("钱包不存在，资金帐户ID：{}，币种类型：{}", moneyAccountId, money.currency().code());
            return new Pair<>(false, null);
        }

        return transactionTemplate.execute(status -> {
            try {
                // 记录操作前的余额
                MoneyBalance oldBalance = balance.copy();

                // 更新余额
                balance.withdraw(money);

                // 持久化余额
                moneyBalanceRepository.save(balance);

                // 创建操作记录和日志
                MoneyBalanceOperation operation = new MoneyBalanceOperation();
                Pair<MoneyBalanceOperation, MoneyBalanceLog> withdrawPair = operation.withdraw(balance, oldBalance,
                        money, fromSource);

                // 保存操作记录
                MoneyBalanceOperation savedOperation = moneyBalanceOperationRepository.save(withdrawPair.getKey());

                // 保存日志
                MoneyBalanceLog moneyBalanceLog = withdrawPair.getValue();
                moneyBalanceLog.setFromSourceType(fromSource.fromSourceType());
                moneyBalanceLog.setFromSourceId(savedOperation.getId());
                moneyBalanceLog.setFromSourceSubId(0L);
                MoneyBalanceLog savedLog = moneyBalanceLogRepository.save(moneyBalanceLog);

                // 发布领域事件
                publishCurrentBalanceChgEvent(MoneyBalanceOperationType.DECREASE.getOperationType(), money,
                        savedLog.getId(), fromSource);

                return new Pair<>(true, savedLog.getId());
            } catch (MoneyNotEnoughException e) {
                log.error("余额不足，资金帐户ID：{}，金额：{}", moneyAccountId, money);
                return new Pair<>(false, null);
            } catch (Exception e) {
                log.error("取现失败，资金帐户ID：{}，金额：{}", moneyAccountId, money, e);
                throw e;
            }
        });
    }

    /**
     * 发布当前余额变更事件
     *
     * @param money          金额
     * @param operationLogId 操作日志ID
     * @param operationType  操作类型
     */
    private void publishCurrentBalanceChgEvent(int operationType, MoneyAmount money, Long operationLogId,
            FromSource fromSource) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                domainEventPublisher.publish(MoneyBalanceChgEvent.builder()
                        // 事件ID
                        .id(System.currentTimeMillis())
                        .moneyAccountId(moneyAccountId)
                        .moneyType(money.currency().code())
                        .operationId(operationLogId)
                        .operationType(operationType)
                        .fromSourceType(fromSource.fromSourceType())
                        .fromSourceId(fromSource.fromSourceId())
                        .fromSourceSubId(fromSource.fromSourceSubId())
                        .chgCurrentBalance(operationType == MoneyBalanceOperationType.INCREASE.getOperationType()
                                ? money.amount()
                                : money.amount().negate())
                        .chgFrozenAmount(BigDecimal.ZERO)
                        .operationTime(LocalDateTime.now())
                        .build());
            }
        });
    }

    /**
     * 查找指定币种的钱包
     *
     * @param moneyType 币种类型
     * @return 钱包
     */
    private MoneyBalance findBalance(int moneyType) {
        return moneyBalances.stream()
                .filter(balance -> balance.getMoneyType() == moneyType)
                .findFirst()
                .orElse(null);
    }

}

package com.oneinstep.ddd.asset.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 钱包变动日志 领域对象
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "money_balance_log")
public class MoneyBalanceLog {

    /**
     * 钱包变动日志ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 资金账户ID
     */
    private Long moneyAccountId;

    /**
     * 币种类型
     */
    private Integer moneyType;

    /**
     * 余额变动
     */
    private BigDecimal chgCurrentBalance;

    /**
     * 操作前余额
     */
    private BigDecimal beforeCurrentBalance;

    /**
     * 操作后余额
     */
    private BigDecimal afterCurrentBalance;

    /**
     * 冻结金额变动
     */
    private BigDecimal chgFrozenAmount;

    /**
     * 操作前冻结金额
     */
    private BigDecimal beforeFrozenAmount;

    /**
     * 操作后冻结金额
     */
    private BigDecimal afterFrozenAmount;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 标记位 00000000000000000000000000000000
     * 每一位表示一个标记 0表示未标记,1表示已标记 可以存储64个标记
     */
    private Long flagBit = 0L;

    /**
     * 来源类型-用于幂等
     */
    private Integer fromSourceType;

    /**
     * 来源ID-用于幂等
     */
    private Long fromSourceId;

    /**
     * 来源子ID-用于幂等
     */
    private Long fromSourceSubId;

    /**
     * 添加标记位
     *
     * @param flagBit 标记位
     */
    public void addFlagBit(long flagBit) {
        this.flagBit = this.flagBit | flagBit;
    }

    /**
     * 判断标记位是否已设置
     *
     * @param flagBit 标记位
     * @return 是否已设置
     */
    public boolean isFlagBitSet(long flagBit) {
        return (this.flagBit & flagBit) != 0;
    }

    /**
     * 从操作记录创建资金变动流水
     *
     * @param operation  操作记录
     * @param newBalance 操作后的余额
     * @param oldBalance 操作前的余额
     * @return 资金变动流水
     */
    public static MoneyBalanceLog fromOperation(MoneyBalanceOperation operation, MoneyBalance newBalance,
                                                MoneyBalance oldBalance) {
        MoneyBalanceLog moneyBalanceLog = new MoneyBalanceLog();
        moneyBalanceLog.setMoneyAccountId(oldBalance.getMoneyAccountId());
        moneyBalanceLog.setOperationTime(LocalDateTime.now());
        moneyBalanceLog.setChgCurrentBalance(oldBalance.getCurrentBalance().subtract(newBalance.getCurrentBalance()));
        moneyBalanceLog.setBeforeCurrentBalance(oldBalance.getCurrentBalance());
        moneyBalanceLog.setAfterCurrentBalance(newBalance.getCurrentBalance());
        moneyBalanceLog.setChgFrozenAmount(BigDecimal.ZERO);
        moneyBalanceLog.setBeforeFrozenAmount(oldBalance.getFrozenAmount());
        moneyBalanceLog.setAfterFrozenAmount(newBalance.getFrozenAmount());
        moneyBalanceLog.setFromSourceType(operation.getFromSourceType());
        return moneyBalanceLog;
    }

}

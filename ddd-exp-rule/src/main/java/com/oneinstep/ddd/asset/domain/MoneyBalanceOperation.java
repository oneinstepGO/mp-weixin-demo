package com.oneinstep.ddd.asset.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.oneinstep.ddd.asset.enums.MoneyBalanceOperationType;
import com.oneinstep.ddd.asset.value.FromSource;
import com.oneinstep.ddd.asset.value.MoneyAmount;

import cn.hutool.core.lang.Pair;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 钱包操作记录 领域对象
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "money_balance_operation")
public class MoneyBalanceOperation {

    /**
     * 操作记录ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 资金账户ID
     */
    private Long moneyAccountId;

    /**
     * 币种类型
     */
    private Integer moneyType;

    /**
     * 操作类型
     */
    private Integer operationType;

    /**
     * 操作金额
     */
    private BigDecimal amount;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 标记位 00000000000000000000000000000000
     */
    private Long flagBit;

    /**
     * 添加标记位
     *
     * @param flagBit 标记位
     */
    public void addFlagBit(long flagBit) {
        this.flagBit |= flagBit;
    }

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
     * 存入资金
     *
     * @param moneyBalance    当前余额
     * @param oldMoneyBalance 操作前的余额
     * @param money           操作金额
     * @return Pair<MoneyBalanceOperation, MoneyBalanceLog> 操作记录和日志
     */
    public Pair<MoneyBalanceOperation, MoneyBalanceLog> deposit(MoneyBalance moneyBalance, MoneyBalance oldMoneyBalance,
                                                                MoneyAmount money, FromSource fromSource) {
        this.moneyAccountId = moneyBalance.getMoneyAccountId();
        this.moneyType = money.currency().code();
        this.amount = money.amount();
        this.operationType = MoneyBalanceOperationType.INCREASE.getOperationType();
        this.operationTime = LocalDateTime.now();
        this.remark = "存入资金";

        this.fromSourceType = fromSource.fromSourceType();
        this.fromSourceId = fromSource.fromSourceId();
        this.fromSourceSubId = fromSource.fromSourceSubId();

        // 创建资金流水
        MoneyBalanceLog moneyBalanceLog = MoneyBalanceLog.fromOperation(this, moneyBalance, oldMoneyBalance);

        return new Pair<>(this, moneyBalanceLog);

    }

    /**
     * 取现
     *
     * @param moneyBalance    当前余额
     * @param oldMoneyBalance 操作前的余额
     * @param money           操作金额
     * @return Pair<MoneyBalanceOperation, MoneyBalanceLog> 操作记录和日志
     */
    public Pair<MoneyBalanceOperation, MoneyBalanceLog> withdraw(MoneyBalance moneyBalance,
                                                                 MoneyBalance oldMoneyBalance,
                                                                 MoneyAmount money,
                                                                 FromSource fromSource) {
        this.moneyAccountId = moneyBalance.getMoneyAccountId();
        this.moneyType = money.currency().code();
        this.amount = money.amount();
        this.operationType = MoneyBalanceOperationType.DECREASE.getOperationType();
        this.operationTime = LocalDateTime.now();
        this.remark = "取现";

        this.fromSourceType = fromSource.fromSourceType();
        this.fromSourceId = fromSource.fromSourceId();
        this.fromSourceSubId = fromSource.fromSourceSubId();

        // 创建资金流水
        MoneyBalanceLog moneyBalanceLog = MoneyBalanceLog.fromOperation(this, moneyBalance, oldMoneyBalance);

        return new Pair<>(this, moneyBalanceLog);
    }

}

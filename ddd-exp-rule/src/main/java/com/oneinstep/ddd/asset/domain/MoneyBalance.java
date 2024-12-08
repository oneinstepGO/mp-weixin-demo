package com.oneinstep.ddd.asset.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;

import com.oneinstep.ddd.asset.exception.MoneyNotEnoughException;
import com.oneinstep.ddd.asset.value.MoneyAmount;

import jakarta.annotation.Nonnull;
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
 * 资金钱包 领域对象
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "money_balance")
public class MoneyBalance {

    /**
     * 钱包ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 资金帐户id
     */
    private Long moneyAccountId;

    /**
     * 币种类型
     */
    private int moneyType;

    /**
     * 当前余额
     */
    private BigDecimal currentBalance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 存钱
     *
     * @param money 金额
     */
    public void deposit(@Nonnull MoneyAmount money) {
        if (money.amount() == null) {
            throw new IllegalArgumentException("Invalid money");
        }
        if (money.currency().code() != this.moneyType) {
            throw new IllegalArgumentException("Money type mismatch");
        }
        this.currentBalance = this.currentBalance.add(money.amount());
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 取钱
     *
     * @param money 金额
     */
    public void withdraw(@Nonnull MoneyAmount money) {
        if (money.amount() == null) {
            throw new IllegalArgumentException("Invalid money");
        }
        if (money.currency().code() != this.moneyType) {
            throw new IllegalArgumentException("Money type mismatch");
        }
        if (!hasSufficientFunds(money.amount())) {
            throw new MoneyNotEnoughException("Insufficient funds");
        }

        this.currentBalance = this.currentBalance.subtract(money.amount());
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 是否有足够的余额
     *
     * @param amount 金额
     * @return 是否有足够的余额
     */
    public boolean hasSufficientFunds(BigDecimal amount) {
        return this.currentBalance.compareTo(amount) >= 0;
    }

    /**
     * 复制
     *
     * @return 复制后的钱包
     */
    public MoneyBalance copy() {
        MoneyBalance moneyBalance = new MoneyBalance();
        BeanUtils.copyProperties(this, moneyBalance);
        return moneyBalance;
    }

}

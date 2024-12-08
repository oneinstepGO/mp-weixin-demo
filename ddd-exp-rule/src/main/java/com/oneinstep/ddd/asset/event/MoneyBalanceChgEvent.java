package com.oneinstep.ddd.asset.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资金余额变更事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneyBalanceChgEvent {

    /**
     * 事件ID
     */
    private Long id;

    /**
     * 资金帐户ID
     */
    private Long moneyAccountId;

    /**
     * 币种类型
     */
    private Integer moneyType;

    /**
     * 操作ID
     */
    private Long operationId;

    /**
     * 操作类型
     */
    private Integer operationType;

    /**
     * 来源类型
     */
    private Integer fromSourceType;

    /**
     * 来源ID
     */
    private Long fromSourceId;

    /**
     * 来源子ID
     */
    private Long fromSourceSubId;

    /**
     * 变更当前余额
     */
    private BigDecimal chgCurrentBalance;

    /**
     * 变更冻结金额
     */
    private BigDecimal chgFrozenAmount;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;
}
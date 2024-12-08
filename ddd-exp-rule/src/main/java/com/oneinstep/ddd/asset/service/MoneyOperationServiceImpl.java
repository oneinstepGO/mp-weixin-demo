package com.oneinstep.ddd.asset.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.oneinstep.ddd.asset.aggregate.MoneyAccount;
import com.oneinstep.ddd.asset.api.money.MoneyOperationService;
import com.oneinstep.ddd.asset.formula.CalRuleManager;
import com.oneinstep.ddd.asset.repository.MoneyAccountRepository;
import com.oneinstep.ddd.asset.util.DistributedLock;
import com.oneinstep.ddd.asset.value.FromSource;
import com.oneinstep.ddd.asset.value.MoneyAmount;
import com.oneinstep.ddd.asset.value.PaymentMethod;

import cn.hutool.core.lang.Pair;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 资金操作服务实现
 */
@Slf4j
@Service
public class MoneyOperationServiceImpl implements MoneyOperationService {

    @Resource
    private DistributedLock distributedLock;
    @Resource
    private MoneyAccountRepository moneyAccountRepository;
    @Resource
    private CalRuleManager calRuleManager;

    @Override
    public Pair<Boolean, Long> deposit(@Nonnull Long moneyAccountId, @Nonnull Integer accountType, @Nonnull MoneyAmount money, @Nonnull FromSource fromSource) {
        // 参数校验
        if (money.amount() == null || money.amount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("参数错误，资金帐户ID：{}，金额：{}", moneyAccountId, money);
            return new Pair<>(false, null);
        }

        // 使用分布式锁保证并发安全
        String lockKey = String.format("money_account_lock_%d_%d", moneyAccountId, money.currency().code());
        return distributedLock.lock(lockKey, () -> {
            try {
                // 获取聚合根
                MoneyAccount moneyAccount = moneyAccountRepository.findById(moneyAccountId);
                if (moneyAccount == null) {
                    log.error("资金帐户不存在，资金帐户ID：{}", moneyAccountId);
                    return new Pair<>(false, null);
                }

                moneyAccount.setAccountType(accountType);

                // 执行存款操作
                return moneyAccount.deposit(money, fromSource);
            } catch (Exception e) {
                log.error("存入资金失败，资金帐户ID：{}，金额：{}", moneyAccountId, money, e);
                return new Pair<>(false, null);
            }
        });
    }

    @Override
    public Pair<Boolean, Long> withdraw(@Nonnull Long moneyAccountId, @Nonnull Integer accountType, @Nonnull MoneyAmount money, @Nonnull FromSource fromSource,
                                        @Nonnull PaymentMethod paymentMethod) {
        // 参数校验
        if (money.amount() == null || money.amount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("参数错误，资金帐户ID：{}，金额：{}", moneyAccountId, money);
            return new Pair<>(false, null);
        }

        // 使用分布式锁保证并发安全
        String lockKey = String.format("money_account_lock_%d_%d", moneyAccountId, money.currency().code());
        return distributedLock.lock(lockKey, () -> {
            try {
                // 获取聚合根
                MoneyAccount moneyAccount = moneyAccountRepository.findById(moneyAccountId);
                if (moneyAccount == null) {
                    log.error("资金帐户不存在，资金帐户ID：{}", moneyAccountId);
                    return new Pair<>(false, null);
                }

                moneyAccount.setAccountType(accountType);

                // 余额验证
                boolean result = calRuleManager.verify(money.currency().code(),
                        paymentMethod.verifyType(),
                        paymentMethod.unitedCredit(),
                        money.amount(), moneyAccount);

                if (!result) {
                    log.error("取现失败，资金帐户ID：{}，金额：{}，支付方式：{}", moneyAccountId, money, paymentMethod);
                    return new Pair<>(false, null);
                }

                // 执行取款操作 不考虑多币种支付逻辑
                return moneyAccount.withdraw(money, fromSource);
            } catch (Exception e) {
                log.error("取现失败，资金帐户ID：{}，金额：{}", moneyAccountId, money, e);
                return new Pair<>(false, null);
            }
        });
    }

}

package com.oneinstep.ddd.asset;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.oneinstep.ddd.asset.api.constants.AssetFromSourceTypeConstant;
import com.oneinstep.ddd.asset.api.money.MoneyOperationService;
import com.oneinstep.ddd.asset.domain.MoneyBalance;
import com.oneinstep.ddd.asset.enums.VerifyTypeConstants;
import com.oneinstep.ddd.asset.formula.AvaWithdrawalCashCalculator;
import com.oneinstep.ddd.asset.formula.AvaWithdrawalUnitedCashCalculator;
import com.oneinstep.ddd.asset.repository.MoneyBalanceRepository;
import com.oneinstep.ddd.asset.value.Currency;
import com.oneinstep.ddd.asset.value.FromSource;
import com.oneinstep.ddd.asset.value.MoneyAmount;
import com.oneinstep.ddd.asset.value.PaymentMethod;

import cn.hutool.core.lang.Pair;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 资金操作测试
 */
@SpringBootTest
@Slf4j
class MoneyOperationServiceTest {

    @Resource
    private MoneyOperationService moneyOperationService;

    @Resource
    private MoneyBalanceRepository moneyBalanceRepository;

    @BeforeEach
    public void init() {
        // create money balance with 500.00
        createMoneyBalance();
    }

    @AfterEach
    public void after() {
        // delete money balance
        moneyBalanceRepository.deleteAll();
    }

    @Test
    void testDeposit() {

        Pair<Boolean, Long> depositPair = moneyOperationService.deposit(1L, 1,
                MoneyAmount.of(Currency.of(1, "USD", "美元"), new BigDecimal("100.00")),
                FromSource.of(AssetFromSourceTypeConstant.CASH_DEPOSIT, 1L, 0L));

        log.info("deposit Pair: {}", depositPair);
        Assertions.assertTrue(depositPair.getKey());
        Assertions.assertNotNull(depositPair.getValue());

    }

    /**
     * 测试 {@link com.oneinstep.ddd.asset.formula.NoVerifyRule}
     * accountType = 1 && 不需要校验 && 可取充足
     */
    @Test
    void testWithdraw_noverify_true() {
        testWithdrawWithVerify(1, new BigDecimal("400.00"),
                PaymentMethod.of(false, VerifyTypeConstants.NO_VERIFY, false),
                true);
    }

    /**
     * 测试 {@link com.oneinstep.ddd.asset.formula.NoVerifyRule}
     * accountType = 1 && 不需要校验 && 可取不足
     */
    @Test
    void testWithdraw_noverify_false() {
        testWithdrawWithVerify(1, new BigDecimal("600.00"),
                PaymentMethod.of(false, VerifyTypeConstants.NO_VERIFY, false),
                false);
    }

    /**
     * 测试 {@link AvaWithdrawalCashCalculator}
     * accountType = 1 && 可取充足
     */
    @Test
    void testWithdraw_AvaWithdrawalCashCalculator_1_true() {

        testWithdrawWithVerify(1, new BigDecimal("200.00"),
                PaymentMethod.of(false, VerifyTypeConstants.VERIFY_AVAILABLE_WITHDRAWAL_BALANCE, false),
                true);
    }

    /**
     * 测试 {@link AvaWithdrawalCashCalculator}
     * accountType = 2 && 可取充足
     */
    @Test
    void testWithdraw_AvaWithdrawalCashCalculator_2_true() {
        testWithdrawWithVerify(1, new BigDecimal("190.00"),
                PaymentMethod.of(false, VerifyTypeConstants.VERIFY_AVAILABLE_WITHDRAWAL_BALANCE, false),
                true);
    }

    /**
     * 测试 {@link AvaWithdrawalCashCalculator}
     * accountType = 1 && 可取不足
     */
    @Test
    void testWithdraw_AvaWithdrawalCashCalculator_1_false() {

        testWithdrawWithVerify(1, new BigDecimal("250.00"),
                PaymentMethod.of(false, VerifyTypeConstants.VERIFY_AVAILABLE_WITHDRAWAL_BALANCE, false),
                false);

    }

    /**
     * 测试 {@link AvaWithdrawalCashCalculator}
     * accountType = 2 && 可取不足
     */
    @Test
    void testWithdraw_AvaWithdrawalCashCalculator_2_false() {

        testWithdrawWithVerify(2, new BigDecimal("200.00"),
                PaymentMethod.of(false, VerifyTypeConstants.VERIFY_AVAILABLE_WITHDRAWAL_BALANCE, false),
                false);

    }

    /**
     * 测试 {@link AvaWithdrawalUnitedCashCalculator}
     * accountType = 1 && 可取充足
     */
    @Test
    void testWithdraw_AvaWithdrawalUnitedCashCalculator_1_true() {

        testWithdrawWithVerify(1, new BigDecimal("170.00"),
                PaymentMethod.of(false, VerifyTypeConstants.VERIFY_AVAILABLE_WITHDRAWAL_BALANCE, true),
                true);

    }

    /**
     * 测试 {@link AvaWithdrawalUnitedCashCalculator}
     * accountType = 1 && 可取不足
     */
    @Test
    void testWithdraw_AvaWithdrawalUnitedCashCalculator_1_false() {

        testWithdrawWithVerify(1, new BigDecimal("180.00"),
                PaymentMethod.of(false, VerifyTypeConstants.VERIFY_AVAILABLE_WITHDRAWAL_BALANCE, true),
                false);

    }

    /**
     * 测试 {@link AvaWithdrawalUnitedCashCalculator}
     * accountType = 2 && 可取充足
     */
    @Test
    void testWithdraw_AvaWithdrawalUnitedCashCalculator_2_true() {

        testWithdrawWithVerify(2, new BigDecimal("190.00"),
                PaymentMethod.of(false, VerifyTypeConstants.VERIFY_AVAILABLE_WITHDRAWAL_BALANCE, true),
                true);

    }

    /**
     * 测试 {@link AvaWithdrawalUnitedCashCalculator}
     * accountType = 2 && 可取不足
     */
    @Test
    void testWithdraw_AvaWithdrawalUnitedCashCalculator_2_false() {

        testWithdrawWithVerify(2, new BigDecimal("195.00"),
                PaymentMethod.of(false, VerifyTypeConstants.VERIFY_AVAILABLE_WITHDRAWAL_BALANCE, true),
                false);

    }

    private void createMoneyBalance() {
        // create money balance
        MoneyBalance moneyBalance = new MoneyBalance();
        moneyBalance.setMoneyAccountId(1L);
        moneyBalance.setMoneyType(1);
        moneyBalance.setCurrentBalance(new BigDecimal("500.00"));
        moneyBalance.setFrozenAmount(new BigDecimal(0));
        moneyBalance.setCreateTime(LocalDateTime.now());
        moneyBalance.setUpdateTime(LocalDateTime.now());
        moneyBalanceRepository.save(moneyBalance);
    }

    void testWithdrawWithVerify(int accountType, BigDecimal amount, PaymentMethod paymentMethod, boolean result) {

        Pair<Boolean, Long> withdrawPair = moneyOperationService.withdraw(1L, accountType,
                MoneyAmount.of(Currency.of(1, "USD", "美元"), amount),
                FromSource.of(AssetFromSourceTypeConstant.CASH_WITHDRAW, 1L, 0L),
                paymentMethod);

        log.info("withdrawPair: {}", withdrawPair);
        if (result) {
            Assertions.assertTrue(withdrawPair.getKey());
            Assertions.assertNotNull(withdrawPair.getValue());
        } else {
            Assertions.assertFalse(withdrawPair.getKey());
            Assertions.assertNull(withdrawPair.getValue());
        }
    }
}

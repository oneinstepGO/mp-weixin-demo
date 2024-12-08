package com.oneinstep.ddd.asset;

import com.oneinstep.ddd.asset.aggregate.MoneyAccount;
import com.oneinstep.ddd.asset.formula.AvaWithdrawalCashCalculator;
import com.oneinstep.ddd.asset.formula.AvaWithdrawalUnitedCashCalculator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.util.Collections;

/**
 * 测试资产计算器
 */
@SpringBootTest
class AbsCalculatorTest {

    @Resource
    private AvaWithdrawalCashCalculator avaWithdrawalCashFormulaCalculator;

    @Resource
    private AvaWithdrawalUnitedCashCalculator avaWithdrawalUnitedCashCalculator;

    @Resource
    private ApplicationContext applicationContext;

    @Test
    void test_AvaWithdrawalCashCalculator_1() {
        MoneyAccount moneyAccount = new MoneyAccount(1L, Collections.emptyList(), applicationContext);
        moneyAccount.setAccountType(1);
        BigDecimal result = avaWithdrawalCashFormulaCalculator.calculate(moneyAccount);

        Assertions.assertNotNull(result);

        Assertions.assertEquals(new BigDecimal("200.00"), result);
    }

    @Test
    void test_AvaWithdrawalCashCalculator_2() {
        MoneyAccount moneyAccount = new MoneyAccount(1L, Collections.emptyList(), applicationContext);
        moneyAccount.setAccountType(2);
        BigDecimal result = avaWithdrawalCashFormulaCalculator.calculate(moneyAccount);

        Assertions.assertNotNull(result);

        Assertions.assertEquals(new BigDecimal("190.00"), result);
    }

    @Test
    void test_AvaWithdrawalUnitedCashCalculator_1() {
        MoneyAccount moneyAccount = new MoneyAccount(1L, Collections.emptyList(), applicationContext);
        moneyAccount.setAccountType(1);
        BigDecimal result = avaWithdrawalUnitedCashCalculator.calculate(moneyAccount);

        Assertions.assertNotNull(result);

        Assertions.assertEquals(new BigDecimal("170.00"), result);
    }

    @Test
    void test_AvaWithdrawalUnitedCashCalculator_2() {
        MoneyAccount moneyAccount = new MoneyAccount(1L, Collections.emptyList(), applicationContext);
        moneyAccount.setAccountType(2);
        BigDecimal result = avaWithdrawalUnitedCashCalculator.calculate(moneyAccount);

        Assertions.assertNotNull(result);

        Assertions.assertEquals(new BigDecimal("190.00"), result);
    }

}

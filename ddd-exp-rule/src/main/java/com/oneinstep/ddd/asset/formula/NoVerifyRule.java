package com.oneinstep.ddd.asset.formula;

import com.oneinstep.ddd.asset.aggregate.MoneyAccount;
import com.oneinstep.ddd.asset.enums.VerifyTypeConstants;
import com.oneinstep.ddd.asset.util.Holder;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.annotation.*;

import java.math.BigDecimal;

import static com.oneinstep.ddd.asset.formula.CalRuleManager.*;

/**
 * 不校验规则
 */
@Rule
@Slf4j
public class NoVerifyRule {

    @Condition
    public boolean shouldUseThisRule(@Fact(VERIFY_TYPE) Integer verifyType) {
        return verifyType == VerifyTypeConstants.NO_VERIFY;
    }

    @Action
    public void doVerify(@Fact(MONEY_ACCOUNT) MoneyAccount moneyAccount,
                         @Fact(VERIFY_AMOUNT) BigDecimal verifyAmount,
                         @Fact(RESULT_HOLDER) Holder<Boolean> resultHolder) {
        log.info("moneyAccount: {}, verifyAmount: {} 不校验", moneyAccount, verifyAmount);
        resultHolder.set(true);
    }

    @Priority
    public int getPriority() {
        return 0;
    }

}

package com.oneinstep.ddd.asset.exception;

/**
 * 提现限额超出异常
 */
public class WithdrawalLimitExceededException extends RuntimeException {

    public WithdrawalLimitExceededException(String message) {
        super(message);
    }
}

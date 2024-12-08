package com.oneinstep.ddd.asset.exception;

/**
 * 钱包不存在异常
 */
public class MoneyBalanceNotExistException extends RuntimeException {

    public MoneyBalanceNotExistException(String message) {
        super(message);
    }

}

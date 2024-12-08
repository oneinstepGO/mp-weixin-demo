package com.oneinstep.ddd.asset.exception;

/**
 * 余额不足异常
 */
public class MoneyNotEnoughException extends RuntimeException {

    public MoneyNotEnoughException(String message) {
        super(message);
    }

}

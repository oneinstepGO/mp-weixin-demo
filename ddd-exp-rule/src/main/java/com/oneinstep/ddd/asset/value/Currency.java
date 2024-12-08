package com.oneinstep.ddd.asset.value;

/**
 * 币种
 * 
 * @param code   币种代码 system_currency_code
 * @param symbol 币种符号
 * @param name   币种名称
 */
public record Currency(int code, String symbol, String name) {

    public static Currency of(int code, String symbol, String name) {
        return new Currency(code, symbol, name);
    }

}

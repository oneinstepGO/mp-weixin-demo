package com.oneinstep.myrpc.core.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * RpcResponse
 */
@Getter
@Setter
@ToString
public class RpcResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -4189080804671234164L;

    /**
     * request id
     */
    private String requestId;
    /**
     * result of the request
     */
    private Object result;
    /**
     * error
     */
    private String error;

}
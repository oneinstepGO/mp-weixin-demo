package com.oneinstep.myrpc.core.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * RpcRequest
 */
@Getter
@Setter
@ToString
public class RpcRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -4355285085441097045L;

    /**
     * request id
     */
    private String requestId;
    /**
     * interface name
     */
    private String className;
    /**
     * method name
     */
    private String methodName;
    /**
     * parameter types
     */
    private Class<?>[] parameterTypes;
    /**
     * parameters
     */
    private Object[] parameters;

    /**
     * service version
     */
    private String version;
}
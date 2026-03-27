package com.bingli.moliapiinterface.util;

public enum ResponseEnum {

    SUCCESS(200, "成功"),
    ERROR(500, "系统错误"),
    DATA_NOT_FOUND(404, "数据不存在"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问");

    private final int code;

    private final String message;

    ResponseEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

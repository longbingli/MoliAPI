package com.bingli.moliapiinterface.util;

import org.springframework.stereotype.Component;

@Component
public class ResultUtils {

    public static <T> Result<T> success(T data) {
        return new Result<>(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResponseEnum.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> error(ResponseEnum responseEnum) {
        return new Result<>(responseEnum.getCode(), responseEnum.getMessage(), null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(ResponseEnum.ERROR.getCode(), message, null);
    }
}

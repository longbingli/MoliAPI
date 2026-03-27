package com.bingli.moliapiinterface.exception;

import com.bingli.moliapiinterface.util.ResponseEnum;
import com.bingli.moliapiinterface.util.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<com.bingli.moliapiinterface.util.Result<?>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        HttpStatus status = mapCodeToStatus(e.getCode());
        return ResponseEntity
                .status(status)
                .body(ResultUtils.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<com.bingli.moliapiinterface.util.Result<?>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("Parameter type mismatch: {} - {}", e.getName(), e.getValue());
        String message = String.format("参数格式错误: '%s' 应为有效的 %s 类型", 
            e.getName(), e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知");
        return ResponseEntity
                .badRequest()
                .body(ResultUtils.error(ResponseEnum.PARAM_ERROR.getCode(), message));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<com.bingli.moliapiinterface.util.Result<?>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("Missing required parameter: {}", e.getParameterName());
        return ResponseEntity
                .badRequest()
                .body(ResultUtils.error(ResponseEnum.PARAM_ERROR.getCode(), "缺少必要参数: " + e.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<com.bingli.moliapiinterface.util.Result<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数验证失败");
        return ResponseEntity
                .badRequest()
                .body(ResultUtils.error(ResponseEnum.PARAM_ERROR.getCode(), message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<com.bingli.moliapiinterface.util.Result<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("Request body parsing failed: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ResultUtils.error(ResponseEnum.PARAM_ERROR.getCode(), "请求体格式错误"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<com.bingli.moliapiinterface.util.Result<?>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("Method not supported: {}", e.getMethod());
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ResultUtils.error(405, "不支持的请求方法: " + e.getMethod()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<com.bingli.moliapiinterface.util.Result<?>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("No handler found: {}", e.getRequestURL());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResultUtils.error(ResponseEnum.DATA_NOT_FOUND));
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<com.bingli.moliapiinterface.util.Result<?>> handleNumberFormatException(NumberFormatException e) {
        log.warn("Number format error: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ResultUtils.error(ResponseEnum.PARAM_ERROR.getCode(), "参数格式错误: 需要有效的数字"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<com.bingli.moliapiinterface.util.Result<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ResultUtils.error(ResponseEnum.PARAM_ERROR.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<com.bingli.moliapiinterface.util.Result<?>> handleException(Exception e) {
        log.error("System exception: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResultUtils.error(ResponseEnum.ERROR));
    }

    private HttpStatus mapCodeToStatus(int code) {
        return switch (code) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}

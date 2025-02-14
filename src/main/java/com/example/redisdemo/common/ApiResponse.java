package com.example.redisdemo.common;

import java.io.Serializable;

public class ApiResponse<T> implements Serializable {
    private int code;       // 状态码
    private String message; // 返回消息
    private T data;         // 返回数据

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // 成功返回
    public static <T> ApiResponse<T> success(int code, T data) {
        return new ApiResponse(code, "Success", data);
    }

    // 失败返回（自定义错误信息）
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}

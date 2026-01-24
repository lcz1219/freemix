package com.freemix.freemix.util;

public class ApiResponse<T> {
    private boolean operSucc; // 操作是否成功
    private String msg; // 消息
    private T data;
    private int code;// 数据
    private java.util.List<com.freemix.freemix.enetiy.Achievement> achievements; // 新解锁的成就

    // 无参构造方法
    public ApiResponse() {
    }

    // 带参构造方法
    public ApiResponse(boolean operSucc, String msg, T data,int code) {
        this.operSucc = operSucc;
        this.msg = msg;
        this.data = data;
        this.code = code;
    }

    public ApiResponse(boolean operSucc, String msg, int code) {
        this.operSucc = operSucc;
        this.msg = msg;
        this.code = code;
    }

    // Getter 和 Setter 方法
    public java.util.List<com.freemix.freemix.enetiy.Achievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(java.util.List<com.freemix.freemix.enetiy.Achievement> achievements) {
        this.achievements = achievements;
    }

    public boolean isOperSucc() {
        return operSucc;
    }

    public void setOperSucc(boolean operSucc) {
        this.operSucc = operSucc;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // 静态方法，用于快速创建成功响应
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data,200);
    }

    public static <T> ApiResponse<T> success(T data, String msg) {
        return new ApiResponse<>(true, msg, data,200);
    }
//    public static <T> ApiResponse<T> success( String msg,T data) {
//        return new ApiResponse<>(true, msg, data,200);
//    }
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, "msg", null,200);
    }

    public static <T> ApiResponse<T> success(String msg) {
        return new ApiResponse<>(true, msg, null,200);
    }
    // 静态方法，用于快速创建失败响应
    public static <T> ApiResponse<T> failure(String msg) {
        return new ApiResponse<>(false, msg, null,500);
    }

    public static <T> ApiResponse<T> failure(String msg, T data) {
        return new ApiResponse<>(false, msg, data,500);
    } public static <T> ApiResponse<T> failure(String msg, Integer code) {
        return new ApiResponse<>(false, msg,code);
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "operSucc=" + operSucc +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
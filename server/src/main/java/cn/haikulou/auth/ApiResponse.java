package cn.haikulou.auth;

/**
 * 统一 API 响应封装。
 *
 * <p>所有对外接口返回此结构，保证前端解析逻辑统一：
 * <pre>
 * {
 *   "code": 0,
 *   "message": "success",
 *   "data": ...
 * }
 * </pre>
 *
 * @param <T> data 字段承载的业务数据类型
 */
public class ApiResponse<T> {

    /** 业务码：0 表示成功，非 0 表示失败 */
    private int code;

    /** 描述信息，面向用户友好文案 */
    private String message;

    /** 业务数据，失败时为 null */
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应工厂方法。
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return code=0 的成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(0, "success", data);
    }

    /**
     * 失败响应工厂方法。
     *
     * @param code    业务错误码
     * @param message 错误描述
     * @param <T>     数据类型
     * @return data=null 的失败响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<T>(code, message, null);
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
}

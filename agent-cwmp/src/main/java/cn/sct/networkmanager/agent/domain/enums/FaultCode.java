package cn.sct.networkmanager.agent.domain.enums;

public enum FaultCode {
    /**
     * 请求方法不支持
     */
    METHOD_NOT_SUPPORTED(8000, "请求方法不支持"),

    /**
     * ACS 临时不可用（要求重试）
     */
    ACS_TEMPORARILY_UNAVAILABLE(8005, "ACS 临时不可用（要求重试）"),

    /**
     * 方法不支持
     */
    METHOD_NOT_SUPPORTED_9000(9000, "方法不支持"),

    /**
     * 请求格式错误
     */
    REQUEST_FORMAT_ERROR(9001, "请求格式错误"),

    /**
     * 参数无效
     */
    INVALID_PARAMETER_9002(9002, "参数无效"),

    /**
     * 无效参数
     */
    INVALID_PARAMETER_9003(9003, "无效参数"),

    /**
     * 参数数量过多
     */
    TOO_MANY_PARAMETERS(9004, "参数数量过多"),

    /**
     * 参数不存在
     */
    PARAMETER_NOT_EXISTS(9005, "参数不存在"),

    /**
     * 参数只读
     */
    PARAMETER_READ_ONLY(9006, "参数只读"),

    /**
     * 参数值无效
     */
    INVALID_PARAMETER_VALUE(9007, "参数值无效"),

    /**
     * 资源不足
     */
    INSUFFICIENT_RESOURCES(9010, "资源不足"),

    /**
     * 文件传输失败
     */
    FILE_TRANSFER_FAILED(9011, "文件传输失败"),

    /**
     * 参数类型错误
     */
    PARAMETER_TYPE_ERROR(9012, "参数类型错误");

    private final int code;
    private final String message;

    FaultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return code + ": " + message;
    }


}

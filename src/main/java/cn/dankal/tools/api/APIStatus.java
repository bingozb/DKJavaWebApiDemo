package cn.dankal.tools.api;

/**
 * API错误枚举
 */
public enum APIStatus {

    API_SUCCESS("00001", "success"),
    API_USER_PASSWORD_ERROR("00002", "用户名密码错误"),
    API_USER_NOT_EXIST("00003", "用户名不存在");

    private String state;
    private String message;

    APIStatus(String state, String message) {
        this.state = state;
        this.message = message;
    }

    public String getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }
}

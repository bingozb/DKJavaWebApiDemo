package cn.dankal.tools.api;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * API响应实体类
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class APIResponse {
    private String state;
    private String message;
    private Object result;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * 获取 APIResponse 单例对象
     *
     * @return 单例API工具对象
     */
    public static APIResponse getInstance() {
        return APIResponseHolder.instance;
    }

    private APIResponse() {
    }

    private static class APIResponseHolder {
        private final static APIResponse instance = new APIResponse();
    }
}

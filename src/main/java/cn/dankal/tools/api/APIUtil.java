package cn.dankal.tools.api;

import static cn.dankal.tools.api.APIStatus.API_SUCCESS;

/**
 * API工具类
 */
public class APIUtil {

    /**
     * 获取 APIResponse 对象
     *
     * @param apiStatus 接口状态枚举值
     * @param result    接口结果
     * @return APIResponse 单例对象
     */
    public static APIResponse getResponse(APIStatus apiStatus, Object result) {
        APIResponse response = APIResponse.getInstance();
        response.setState(apiStatus.getState());
        response.setMessage(apiStatus.getMessage());
        response.setResult(apiStatus == API_SUCCESS ? result : null);

        return response;
    }
}
package cn.dankal.tools.api;

/**
 * Created by bingo on 2017/4/1.
 */
public class APIUtil {

    private APIResponse response;

    public static APIResponse getResponse(String state, String message, Object result) {
        APIResponse response = APIResponse.getInstance();
        response.setState(state);
        response.setMessage(message);
        response.setResult(result);

        return response;
    }
}
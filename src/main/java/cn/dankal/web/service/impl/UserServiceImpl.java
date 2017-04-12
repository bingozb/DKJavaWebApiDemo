package cn.dankal.web.service.impl;

import javax.annotation.Resource;

import cn.dankal.tools.api.APIRequest;
import cn.dankal.tools.api.APIResponse;
import cn.dankal.tools.api.APIUtil;
import org.springframework.stereotype.Service;

import cn.dankal.web.mapper.UserMapper;
import cn.dankal.web.model.User;
import cn.dankal.web.service.UserService;
import cn.dankal.tools.md5.MD5Util;

import java.util.List;

import cn.dankal.tools.api.APIStatus;

import static cn.dankal.tools.api.APIStatus.API_SUCCESS;
import static cn.dankal.tools.api.APIStatus.API_USER_PASSWORD_ERROR;
import static cn.dankal.tools.api.APIStatus.API_USER_NOT_EXIST;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper mapper;

    public APIResponse login(APIRequest request) {
        // 获取请求参数
        String username = (String) request.getParameter("username");
        String password = (String) request.getParameter("password");
        // 处理业务逻辑
        User user = mapper.selectUserByUsername(username);
        APIStatus status = API_SUCCESS;
        if (user == null) {
            status = API_USER_NOT_EXIST;
        } else {
            if (!MD5Util.md5(password).equals(user.getPassword()))
                status = API_USER_PASSWORD_ERROR;
        }
        // 返回APIResponse对象，配合@ResponseBody转为Json
        return APIUtil.getResponse(status, user);
    }

    public APIResponse allUsers() {
        List<User> users = mapper.selectAllUser();
        return APIUtil.getResponse(API_SUCCESS, users);
    }
}

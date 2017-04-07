package cn.dankal.web.service.impl;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import cn.dankal.tools.api.APIResponse;
import cn.dankal.tools.api.APIUtil;
import org.springframework.stereotype.Service;

import cn.dankal.web.mapper.UserMapper;
import cn.dankal.web.model.User;
import cn.dankal.web.service.UserService;
import cn.dankal.tools.md5.MD5Util;

import java.util.List;

import cn.dankal.tools.api.APIStatus;

import static cn.dankal.tools.api.APIStatus.API_PASSWORD_ERROR;
import static cn.dankal.tools.api.APIStatus.API_SUCCESS;
import static cn.dankal.tools.api.APIStatus.API_USER_NOT_EXIST;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper mapper;

    public APIResponse login(HttpServletRequest request) {
        User user = new User(request.getParameter("username"), request.getParameter("password"));
        String password = MD5Util.MD5(user.getPassword());
        user = mapper.selectUserByUsername(user.getUsername());
        APIStatus status = API_SUCCESS;
        if (user == null) {
            status = API_USER_NOT_EXIST;
        } else {
            if (!password.equals(user.getPassword())) {
                status = API_PASSWORD_ERROR;
            }
        }

        return APIUtil.getResponse(status.getState(), status.getMessage(), status == API_SUCCESS ? user : null);
    }

    public APIResponse allUsers() {
        List<User> users = mapper.selectAllUser();
        return APIUtil.getResponse(API_SUCCESS.getState(), API_SUCCESS.getMessage(), users);
    }
}

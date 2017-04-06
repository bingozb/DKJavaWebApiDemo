package cn.dankal.web.service;

import cn.dankal.tools.api.APIStatus;
import cn.dankal.web.model.User;
import cn.dankal.tools.api.*;

import java.util.List;

public interface UserService {

    /**
     * 登录
     */
    APIResponse login(User user);

    /**
     * 获取所有用户
     */
    APIResponse allUsers();
}

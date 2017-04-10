package cn.dankal.web.service;

import cn.dankal.tools.api.*;

public interface UserService {

    /**
     * 登录
     */
    APIResponse login(APIRequest request);

    /**
     * 获取所有用户
     */
    APIResponse allUsers();
}

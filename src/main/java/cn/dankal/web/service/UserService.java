package cn.dankal.web.service;

import cn.dankal.tools.api.*;

import javax.servlet.http.HttpServletRequest;

public interface UserService {

    /**
     * 登录
     */
    APIResponse login(HttpServletRequest request);

    /**
     * 获取所有用户
     */
    APIResponse allUsers();
}

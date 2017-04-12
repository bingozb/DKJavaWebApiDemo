package cn.dankal.web.service;

import cn.dankal.tools.api.APIRequest;
import cn.dankal.tools.api.APIResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-mybatis.xml"})
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void login() throws Exception {
        // 正常登录
        APIRequest request = new APIRequest();
        request.setAttribute("username", "bingo");
        request.setAttribute("password", "123456");
        APIResponse response = userService.login(request);
        Assert.assertTrue(response.getMessage(), response.getState().equals("00001"));

        // 密码错误
        request.setAttribute("username", "bingo");
        request.setAttribute("password", "1234567");
        response = userService.login(request);
        Assert.assertTrue(response.getMessage(), response.getState().equals("00002"));

        // 用户名不存在
        request.setAttribute("username", "bingo1");
        request.setAttribute("password", "123456");
        response = userService.login(request);
        Assert.assertTrue(response.getMessage(), response.getState().equals("00003"));
    }

    @Test
    public void allUsers() throws Exception {
        APIResponse response = userService.allUsers();
        Assert.assertTrue(response.getMessage(), response.getState().equals("00001"));
    }
}
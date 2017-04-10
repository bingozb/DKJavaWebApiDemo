package cn.dankal.web.service;

import cn.dankal.tools.api.APIRequest;
import cn.dankal.tools.api.APIResponse;
import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-mybatis.xml"})
public class UserServiceTest {

    private Logger logger = Logger.getLogger(this.getClass());

    @Resource
    private UserService userService;

    @Test
    public void login() throws Exception {
        APIRequest request = new APIRequest();
        request.setAttribute("username", "bingo");
        request.setAttribute("password", "123456");
        APIResponse response = userService.login(request);
        assertTrue(response);
    }

    @Test
    public void allUsers() throws Exception {
        APIResponse response = userService.allUsers();
        assertTrue(response);
    }

    private void assertTrue(APIResponse response) {
        Gson gson = new Gson();
        logger.info(gson.toJson(response));

        Assert.assertTrue(response.getMessage(), response.getState().equals("00001"));
    }
}
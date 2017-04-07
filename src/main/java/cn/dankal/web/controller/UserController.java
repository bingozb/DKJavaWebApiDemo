package cn.dankal.web.controller;

import cn.dankal.tools.api.APIResponse;
import cn.dankal.web.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequestMapping("user")
@Controller
public class UserController {

    @Resource
    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.POST, headers = "api-version=1")
    public @ResponseBody
    APIResponse login(HttpServletRequest request) {
        return userService.login(request);
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET, headers = "api-version=1")
    public @ResponseBody
    APIResponse query() {
        return userService.allUsers();
    }
}

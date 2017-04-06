package cn.dankal.web.controller;

import cn.dankal.tools.api.APIResponse;
import cn.dankal.web.model.User;
import cn.dankal.web.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequestMapping("/user")
@Controller
public class UserController {

    @Resource
    private UserService userService;

    @RequestMapping("/login")
    public @ResponseBody
    APIResponse login(HttpServletRequest request, HttpServletResponse response) {
        User user = new User(request.getParameter("username"), request.getParameter("password"));
        return userService.login(user);
    }

    @RequestMapping("/quary")
    public @ResponseBody
    APIResponse quary(HttpServletRequest request, HttpServletResponse response) {
        return userService.allUsers();
    }
}

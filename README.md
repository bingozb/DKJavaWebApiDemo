# DKJavaWebApiDemo
个人 JavaWeb 写 API 的实现设计，Maven 管理依赖，框架为 SSM。使用 Spring 的 Jackson 把 POJO 转为 JSON。

## 前言

当后台项目使用 JavaWeb 进行开发时，写接口就成为一门艺术活。无论是后台管理系统的前后端分离、还是要给移动端提供数据，返回方便、规范的数据及其重要。掌握以后，你肯定会爱不释手。有一年以上 Java 开发经验的人，相信不会再选择 PHP、Nodejs 等来写接口，原因只能意会，不可言传。

## 规定与约束

- 开发工具：IntelliJ IDEA

- 开发语言：Java

- 依赖管理：Maven

- 整合框架：Spring 4.1.4 + SpringMVC 4.1.4 + Mybatis 3.4.1

- 架构：MMCS（Mapper、Model、Controller、Service）

    - mapper 是对象持久化映射层，使用 mybatis 进行数据库交互;
    - controller 是控制层，相当于 MVC 的 C层;
    - model 是数据模型层相当于 MVC 的 M层，存放 POJO 类;
    - servive 是一些业务逻辑的处理层;
    
## 核心配置

三大框架的整合网上例子很多，这里只阐述针对写 API 的核心点。

### 依赖 Jackson

pom.xml 添加 jackson 依赖

```xml
<dependency>
    <groupId>org.codehaus.jackson</groupId>
    <artifactId>jackson-mapper-asl</artifactId>
    <version>1.9.13</version>
</dependency>
```

### Spring MVC 配置

修改 springmvc 的配置文件，对应 Demo 中的 springmvc-servlet.xml。

#### 开启注解模式驱动
```xml
<mvc:annotation-driven/>
```

添加这个标签相当于注册了 DefaultAnnotationHandlerMapping 和 AnnotationMethodHandlerAdapter 两个 bean，配置一些 messageconverter，解决了 @Controller 注解的使用前提配置。

由于 JSON 需要配置 AnnotationMethodHandlerAdapter 和 MappingJacksonHttpMessageConverter，设置此标签后，就不需要再配置了。

#### 删除视图解析器

由于只提供 API 服务，不需要做页面跳转，所不需要视图解析器（ViewResolver），相比普通的 Web 项目，可以**不配置**这个 bean。

```xml
<bean  
   class="org.springframework.web.servlet.view.UrlBasedViewResolver">  
   <property name="suffix" value=".jsp" />  
   ... 
</bean> 
```

## 实现设计

### Json数据格式规范

#### 正确结果的响应
```json
{
    "state": "00001", 
    "message": "success", 
    "result": {
        "id": 1,
        "username": "bingo",
        "enable": true,
        "create_time": "1483082840732"
    }
}
```

#### 错误结果的响应
```json
{
    "state": "00002", 
    "message": "用户名密码错误"
}
```

#### 字段说明

字段 | 说明
---|---
state | 状态码，例如 00001 为成功状态，其它状态码为错误码
message | 消息提示，例如 success 为成功，其它情况为错误原因
result | 结果数据

### API工具类封装

#### APIStatus

由于状态对应的状态码和消息都属于常量，所以抽成一个文件来统一管理枚举。

```java
/**
 * API状态枚举
 */
public enum APIStatus {

    API_SUCCESS("00001", "success"),
    API_USER_PASSWORD_ERROR("00002", "用户名密码错误"),
    API_USER_NOT_EXIST("00003", "用户名不存在");

    private String state;
    private String message;

    APIStatus(String state, String message) {
        this.state = state;
        this.message = message;
    }

    public String getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }
}

```

#### APIResponse

```java
/**
 * API响应实体类
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class APIResponse {
    private String state;
    private String message;
    private Object result;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * 获取 APIResponse 单例对象
     *
     * @return 单例API工具对象
     */
    public static APIResponse getInstance() {
        return APIResponseHolder.instance;
    }

    private APIResponse() {}

    private static class APIResponseHolder {
        private final static APIResponse instance = new APIResponse();
    }
}
```

@JsonSerialize 注解用于属性或者 getter 方法上，用于在序列化时嵌入我们自定义的代码。这里设置 APIResponse 对象转为 Json 时不包含空值，也就是不会出现值为 null 的键值对。

#### APIUtil

```java
/**
 * API工具类
 */
public class APIUtil {

    /**
     * 获取 APIResponse 对象
     *
     * @param apiStatus 接口状态枚举值
     * @param result    接口结果
     * @return APIResponse 单例对象
     */
    public static APIResponse getResponse(APIStatus apiStatus, Object result) {
        APIResponse response = APIResponse.getInstance();
        response.setState(apiStatus.getState());
        response.setMessage(apiStatus.getMessage());
        response.setResult(apiStatus == API_SUCCESS ? result : null);

        return response;
    }
}
```

### Mapper层设计

与普通的 Mapper 一样，Demo 中我为了简便，使用了 Mybatis 的注解的方式，做数据库查询操作。

```java
public interface UserMapper {

    @Select("select * from user where username=#{username}")
    User selectUserByUsername(@Param("username") String username);

    @Select("select * from user")
    List<User> selectAllUser();
}
```

### Service层设计

#### 接口

- 返回值

    Service 层所有的方法统一返回 APIResponse 对象。

- 参数
    - 当接口请求有传值时，方法的参数为 HttpServletRequest 对象；
    - 当接口请求不需要传值时，一般为 GET 请求，此时方法不需要参数。

eg.

```java
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
```

#### 实现

实现类是整个架构中代码量最多的一部分，所有的业务逻辑处理全部都放在 ServiceImpl，包括请求参数值的获取、业务逻辑处理、最后返回 APIResponse 对象。

```java
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper mapper;

    public APIResponse login(HttpServletRequest request) {
        // 获取请求参数
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        // 处理业务逻辑
        User user = new User(username, password);
        user = mapper.selectUserByUsername(user.getUsername());
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

```

### Controller 层设计

基于前面的设计，此时的 Controller 层变得非常轻量级。

```java
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
```

其中，@RequestMapping 除了绑定路由，headers 还规定一定要有 `api-version=1`（参考） 这对键值对，这是接口的版本控制，在迭代开发中是非常重要的。

方法的返回值为 APIResponse，要加上注解 `@ResponseBody`，作用是将返回的对象作为 HTTP 响应正文返回，并调用适合 HttpMessageConverter 的 Adapter 转换对象，写入输出流。由于前面已经配置了`<mvc:annotation-driven />`，就由 Spring 指定了 Converter 为 MappingJacksonHttpMessageConverter，所以需要依赖 Jackson 就是这个原因。

## 效果

至此，整套写 API 的框架就整合配置完成了，测试效果符合预期。

![Demo](https://github.com/bingozb/DKJavaWebApiDemo/blob/master/demo.jpg)

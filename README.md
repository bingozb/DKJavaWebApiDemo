# DKJavaWebApiDemo
个人 JavaWeb 写 API 的实现设计，Maven 管理依赖，框架为 Spring 4.1.4 + SpringMVC 4.1.4 + Mybatis 3.4.1，序列化使用 Gson 2.8。

## 前言

当后台项目使用 JavaWeb 进行开发时，写接口就成为一门艺术活。无论是后台管理系统的前后端分离、还是要给移动端提供数据，返回方便、规范的数据及其重要。掌握以后，你肯定会爱不释手。有一年以上 Java 开发经验的人，相信不会再选择 PHP、Nodejs 等来写接口，原因只能意会，不可言传。

<!-- more -->

## 规定与约束

- 开发工具：IntelliJ IDEA

- 开发语言：Java

- 依赖管理：Maven

- 整合框架：Spring 4.1.4 + SpringMVC 4.1.4 + Mybatis 3.4.1

- 架构：MMCS（Mapper、Model、Controller、Service）

    - Mapper 是对象持久化映射层，使用 Mybatis 进行数据库交互
    - Model 是数据模型层相当于 MVC 的 M层，存放 POJO 类
    - Controller 是控制层，相当于 MVC 的 C层
    - Servive 是一些业务逻辑的处理层
    
## 核心配置

三大框架的整合网上例子很多，这里只阐述针对写 API 的核心点。

### 添加依赖

pom.xml 添加 Gson 依赖

```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.8.0</version>
</dependency>
```

添加 Aspectj 依赖。

```xml
<!-- AOP -->
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.7.4</version>
</dependency>
<dependency>
    <groupId>aspectj</groupId>
    <artifactId>aspectjrt</artifactId>
    <version>1.5.4</version>
</dependency>
```

### Spring MVC 配置

修改 SpringMVC 的配置文件，对应 Demo 中的 spring-mvc.xml。

#### 开启注解模式驱动

```xml
<mvc:annotation-driven/>
```

由于 JSON 需要配置 AnnotationMethodHandlerAdapter 和合适的 HttpMessageConverter，Spring 4.1 提供了 Gson 的 HttpMessageConverter，设置此标签后，并且已经依赖了 Gson，就已经完成了 JSON 的配置。

相当于注册了 DefaultAnnotationHandlerMapping 和 AnnotationMethodHandlerAdapter 两个 bean，配置一些 messageConverter 为 GsonHttpMessageConverter，解决了 @Controller 注解的使用前提配置。

#### 启动自动扫包

```xml
<context:component-scan base-package="cn.dankal.web">
    <!-- 制定扫包规则,不扫描使用@Service注解的JAVA类 -->
    <context:exclude-filter type="annotation" expression="org.springframework.stereotype.Service"/>
</context:component-scan>
```

注意，这里不能扫描`@Service`注解的 Java 类，因为 `spring-mvc.xml` 与 `spring-mybatis.xml` 不是同时加载，如果不进行这样的设置，那么 spring 就会将所有带 `@Service` 注解的类都扫描到容器中，等到加载 `spring-mybatis.xml` 的时候，会因为容器已经存在 Service 类，使得 cglib 不对 Service 进行代理，直接导致的结果就是在 `spring-mybatis.xml` 中的事务配置不起作用，发生异常时，无法对数据进行回滚，在这里也会导致 APILogger 无法正常切入。

#### 删除视图解析器

由于只提供 API 服务，不需要做页面跳转，所不需要视图解析器（ViewResolver），相比普通的 Web 项目，可以**不配置**这个 bean。

```xml
<bean  
   class="org.springframework.web.servlet.view.InternalResourceViewResolver">  
   <property name="suffix" value=".jsp" />  
   ... 
</bean> 
```

### Spring 配置

启动 Aspectj 注解模式驱动 AOP

```xml
<aop:aspectj-autoproxy proxy-target-class="true"/>
```

`proxy-target-class="true"` 是强制使用 CGLIB 进行动态代理，如果不添加，则是 JDK 动态代理，反射的效率不是很高。

使用 CGLib 实现动态代理，完全不受代理类必须实现接口的限制，而且 CGLib 底层采用 ASM 字节码生成框架，使用字节码技术生成代理类，比使用 Java 反射效率要高。唯一需要注意的是，CGLib 不能对声明为 final 的方法进行代理，因为 CGLib 原理是动态生成被代理类的子类。

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

#### APIRequest

APIRequest 对 HttpServletRequest 进行封装，提供常用的属性包括请求的URL、请求参数等。如果提供的属性不能满足需求，还提供了原 HttpServletRequest 对象属性 request。

```java
/**
 * API请求实体类
 */
public class APIRequest {

    /** Raw Request, 单元测试时为null */
    private HttpServletRequest request;

    /** 请求头 */
    private Map<String, Object> header;

    /** 请求参数 */
    private Map<String, Object> params;

    /** 客户端发出请求时的完整URL */
    private String url;

    /** 请求行中的资源名部分 */
    private String uri;

    /** 请求行中的参数部分 */
    private String queryString;

    /** 请求方法 */
    private String method;

    /** 发出请求的客户机的IP地址 */
    private String remoteAddr;

    /** 发出请求的客户机的完整主机名 */
    private String remoteHost;

    /** 发出请求的客户机的网络端口号 */
    private int remotePort;

    /** WEB服务器的IP地址 */
    private String localAddr;

    /** WEB服务器的主机名 */
    private String localName;

    /** WEB服务器的网络端口号 */
    private int localPort;

    /** 编码格式 */
    private String characterEncoding;

    /** 上下文 */
    private String contextPath;

    public APIRequest() {
        super();
    }

    public APIRequest(HttpServletRequest request) {

        this.request = request;

        header = new HashMap<String, Object>();
        Enumeration e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            Object value = request.getHeader(name);
            header.put(name, value);
        }

        params = getParamesMap(request.getParameterMap());
        url = request.getRequestURL().toString();
        uri = request.getRequestURI();
        remoteAddr = request.getRemoteAddr();
        remoteHost = request.getRemoteHost();
        remotePort = request.getRemotePort();
        contextPath = request.getContextPath();
        localAddr = request.getLocalAddr();
        characterEncoding = request.getCharacterEncoding();
        localName = request.getLocalName();
        localPort = request.getLocalPort();
        method = request.getMethod();
        queryString = request.getQueryString();
    }

    private Map<String, Object> getParamesMap(Map properties) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Iterator entries = properties.entrySet().iterator();
        Map.Entry entry;
        String name = "";
        String value = "";
        while (entries.hasNext()) {
            entry = (Map.Entry) entries.next();
            name = (String) entry.getKey();
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (String v : values) {
                    value = v + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = valueObj.toString();
            }
            returnMap.put(name, value);
        }
        return returnMap;
    }

    public Object getParameter(String s) {
        return params.get(s);
    }

    public void setAttribute(String s, Object o) {
        if (params == null) params = new HashMap<String, Object>();
        params.put(s, o.toString());
    }

    // getters and setters ...
}
```

#### APIResponse

把 API 响应数据封装为实体类，并提供了单例对象的获取方法。

```java
/**
 * API响应实体类
 */
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

APIUtil 作为工具类，原本我封装了 Gson 的相关方法，但后来使用 Spring 的 @ResponseBody 就只需要返回一个 POJO 了，所以，该工具类暂时只提供一个方法。

值得一提的是，返回的 APIResponse 是一个单例对象，虽然 Java 有垃圾自动回收机制，但我个人还是觉得没有必要每个 API 请求都 new 一个 APIResponse 来转换 JSON，用单例会比较合适。

#### APILogger

使用 AOP 技术，编写 Service 层的切面，切点为 `cn.dankal.web.service` 包及子包下的文件。

```java
/**
 * API日志管理器
 * AOP技术 Service层的日志切面
 */
@Component
@Aspect
public class APILogger {
    private Logger logger = Logger.getLogger(this.getClass());

    @Pointcut("execution(* cn.dankal.web.service..*(..))")
    public void apiPointcut() {}

    // 前置通知
    @Before("apiPointcut()")
    public void before(JoinPoint joinPoint) {
        Object obj[] = joinPoint.getArgs();
        if (obj.length > 0) {
            APIRequest request = (APIRequest) obj[0];
            Set set = request.getParams().entrySet();
            Map.Entry[] entries = (Map.Entry[]) set.toArray(new Map.Entry[set.size()]);
            for (Map.Entry entry : entries) {
                logger.info("[Params] " + entry.getKey() + ":" + entry.getValue());
            }
        } else {
            logger.info("[Params] null");
        }
    }

    // 后置返回通知
    @AfterReturning(pointcut = "apiPointcut()", argNames = "joinPoint, response", returning = "response")
    public void afterReturn(JoinPoint joinPoint, APIResponse response) {
        logger.info(joinPoint + " Response: " + new Gson().toJson(response) + "\n");
    }

    // 抛出异常后通知
    @AfterThrowing(pointcut = "apiPointcut()", throwing = "ex")
    public void afterThrow(JoinPoint joinPoint, Exception ex) {
        logger.error(joinPoint + " Exception: " + ex.getMessage());
    }
}
```

在前置通知（@Before）中，将 Service 层的方法的参数进行日志打印。由于已经设计了所有 Service 层的方法的参数都是 `无` 或者 `APIRequest对象`，所以在前置通知中通过连接点拿到的参数，如果有值，必然为 APIRequest 对象，然后遍历打印它的 params 属性输出请求参数。

在后置返回通知（@AfterReturning）中，将 Service 层的方法的返回值进行日志打印。由于已经设计了所有 Service 层的方法的返回值都是 APIResponse 对象，所以可以直接用 Gson 将其序列化为 Json 字符串并打印输出。

在抛出异常后的通知（@AfterThrowing）中，将连接点和异常进行日志打印。

由此，所有的 Service 层的方法都会在执行后输出日志，包括接口请求的参数和响应的结果 Json，而 Service 层不需要写任何 log 语句。

### Model层设计

与普通的 POJO 类一样，包含了数据库表对应的字段，值得一提的是 `transient` 这个关键字，是 Gson 中过滤序列化/反序列化的一种方法。

例如，用户的密码在登录的 API 不参与序列化，可以给 `password` 这个属性添加 `transient` 关键字。

```java
public class User {
    private Integer id;
    private String username;
    private transient String password;
    private Boolean enable;
    private String role;
    private String last_time;
    private String create_time;

    public User() {
        super();
    }

    public User(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    // getters and setters ...
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
    - 当接口请求有传值时，方法的参数为 APIRequest 对象；
    - 当接口请求不需要传值时，一般为 GET 请求，此时方法不需要参数。

eg.

```java
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
```

#### 实现

实现类是整个架构中代码量最多的一部分，所有的业务逻辑处理全部都放在 ServiceImpl，包括请求参数值的获取、业务逻辑处理、最后返回 APIResponse 对象。

```java
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper mapper;

    public APIResponse login(APIRequest request) {
        // 获取请求参数
        String username = (String)request.getParameter("username");
        String password = (String)request.getParameter("password");
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
        return userService.login(new APIRequest(request));
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET, headers = "api-version=1")
    public @ResponseBody
    APIResponse query() {
        return userService.allUsers();
    }
}
```

其中，@RequestMapping 除了绑定路由，headers 还规定一定要有 `api-version=1`（参考） 这对键值对，这是接口的版本控制，在迭代开发中是非常重要的。

方法的返回值为 APIResponse，要加上注解 `@ResponseBody`，作用是将返回的对象作为 HTTP 响应正文返回，并调用 GsonHttpMessageConverter 这个适配器转换对象写入输出流。

**为什么不直接将 HttpServletRequest 对象传给 Service 层方法呢？**

因为 HttpServletRequest 是接口，SUN 定义了 J2EE 的所有接口，由各个 Application Server 的厂商自己实现。HttpServletRequest 本身并没有构造方法，无法实例化，必须在容器运行环境的情况下才能拿到它。

所以，APIRequest 对 HttpServletRequest 对象进行封装，对其添加构造方法，使得我们可以实例化一个 APIRequest 对象，可以在容器（Tomcat）不运行的情况下可以进行单元测试。

## 单元测试

进行 Service 层的单元测试，跑登录的测试用例，并根据期望结果进行断言。

```java
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
```

测试通过，结合 APILogger 自动打印输出 API 日志：

```vim
[INFO][main][2017-04-12 21:15:09][cn.dankal.tools.api.APILogger] - [Params] password:123456
[INFO][main][2017-04-12 21:15:09][cn.dankal.tools.api.APILogger] - [Params] username:bingo
[INFO][main][2017-04-12 21:15:09][cn.dankal.tools.api.APILogger] - execution(APIResponse cn.dankal.web.service.impl.UserServiceImpl.login(APIRequest)) Response: {"state":"00001","message":"success","result":{"id":1,"username":"bingo","enable":true,"role":"管理员","last_time":"1483673751744","create_time":"1483082840732"}}

[INFO][main][2017-04-12 21:15:09][cn.dankal.tools.api.APILogger] - [Params] password:1234567
[INFO][main][2017-04-12 21:15:09][cn.dankal.tools.api.APILogger] - [Params] username:bingo
[INFO][main][2017-04-12 21:15:09][cn.dankal.tools.api.APILogger] - execution(APIResponse cn.dankal.web.service.impl.UserServiceImpl.login(APIRequest)) Response: {"state":"00002","message":"用户名密码错误"}

[INFO][main][2017-04-12 21:15:09][cn.dankal.tools.api.APILogger] - [Params] password:123456
[INFO][main][2017-04-12 21:15:09][cn.dankal.tools.api.APILogger] - [Params] username:bingo1
[INFO][main][2017-04-12 21:15:09][cn.dankal.tools.api.APILogger] - execution(APIResponse cn.dankal.web.service.impl.UserServiceImpl.login(APIRequest)) Response: {"state":"00003","message":"用户名不存在"}
```

每个接口访问都自动输出了日志，请求参数和结果都一目了然，打印的连接点的信息也包含了 Service 层的 API 信息，基本满足了 API 系统 的日志需求。

## 效果

至此，整套写 API 的框架就整合配置完成了，测试效果符合预期。

![Demo](https://github.com/bingozb/DKJavaWebApiDemo/raw/master/src/main/webapp/images/demo.png)

## 后话

Demo 源码已经托管到 [GitHub-DKJavaWebApiDemo](https://github.com/bingozb/DKJavaWebApiDemo)，遵循 MIT 开源协议。一方面作为个人的战斗记录，另一方面，也准备为公司的后台开创一个 JavaWeb 组，这是我今年的计划，还在评估阶段。如果这个设计对您有所帮助，希望能顺手点个 Star，谢谢！
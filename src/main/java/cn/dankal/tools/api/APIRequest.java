package cn.dankal.tools.api;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * API请求实体类
 */
public class APIRequest {

    /**
     * Raw Request, 单元测试时为null
     */
    private HttpServletRequest request;

    /**
     * 请求头
     */
    private Map<String, Object> header;

    /**
     * 请求参数
     */
    private Map<String, Object> params;

    /**
     * 客户端发出请求时的完整URL
     */
    private String url;

    /**
     * 请求行中的资源名部分
     */
    private String uri;

    /**
     * 请求行中的参数部分
     */
    private String queryString;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 发出请求的客户机的IP地址
     */
    private String remoteAddr;

    /**
     * 发出请求的客户机的完整主机名
     */
    private String remoteHost;

    /**
     * 发出请求的客户机的网络端口号
     */
    private int remotePort;

    /**
     * WEB服务器的IP地址
     */
    private String localAddr;

    /**
     * WEB服务器的主机名
     */
    private String localName;

    /**
     * WEB服务器的网络端口号
     */
    private int localPort;

    /**
     * 编码格式
     */
    private String characterEncoding;

    /**
     * 上下文
     */
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
        String name;
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

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public Map<String, Object> getHeader() {
        return header;
    }

    public void setHeader(Map<String, Object> header) {
        this.header = header;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getLocalAddr() {
        return localAddr;
    }

    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}
package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
@SuppressWarnings("all")
public class AuthGlobalFilter implements GlobalFilter, Ordered {


    // 路径匹配器
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    // 获取白名单列表
    @Value("${authUrls.url}")
    private String authUrls;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String LOGIN_PREFIX = "user:login:";

    /**
     * 自定义登录校验逻辑
     *
     * @param exchange：可以获取请求信息、响应信息、请求头、请求参数等
     * @param chain：过滤器链，可以继续执行下一个过滤器或最终路由
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取请求报文信息和响应报文信息
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 获取请求路径
        String path = request.getURI().getPath();
        log.info("获取请求路径: path={}", path);
        // 1、内部接口拒绝直接访问
        if (antPathMatcher.match("/api/**/inner/**", path)) {
            log.info("内部接口拒绝访问: path={}", path);
            return outResult(response, ResultCodeEnum.PERMISSION);
        }
        // 获取用户ID，判断是否登录
        String userId = getUserId(request);

        // userId = -1 表示token被盗用
        if ("-1".equals(userId)) {
            log.info("token被盗用: path={}", path);
            return outResult(response, ResultCodeEnum.ILLEGAL_REQUEST);
        }
        // 2、路径中有auth，需要进行登录才能访问
        if (antPathMatcher.match("/api/**/auth/**", path) && StringUtils.isBlank(userId)) {
            log.info("路径中有auth，需要进行登录才能访问: path={}", path);
            // 未登录，返回用户没有权限登录
            return outResult(response, ResultCodeEnum.LOGIN_AUTH);
        }
        // 3、判断白名单路径，包含白名单路径直接放行
        if (StringUtils.isNotBlank(authUrls)) {
            String[] authUrlArr = authUrls.split(",");
            // 遍历白名单路径
            for (String authUrl : authUrlArr) {
                // 判断是否包含在白名单内，并且用户没有登录，则返回用户未登录状态，重定向到登录页面
                if (path.indexOf(authUrl) != -1 && StringUtils.isBlank(userId)) {
                    return redirectLogin(request, response);
                }
            }
        }
        // 获取临时用户id（未登录状态下）
        String tempUserId = getTempUserId(request);
        // 封装userId到请求头中，后续的服务调用可以获取userId信息
        if (StringUtils.isNotBlank(userId) || StringUtils.isNotBlank(tempUserId)) {
            if (StringUtils.isNotBlank(userId)) {
                request.mutate().header("userId", userId).build();
            }
            if (StringUtils.isNotBlank(tempUserId)) {
                request.mutate().header("userTempId", tempUserId).build();
            }
            return chain.filter(exchange.mutate().request(request).build());
        }
        // 放行请求
        return chain.filter(exchange);

    }

    /**
     * 获取临时用户id（未登录状态下）
     * @param request
     * @param response
     * @return
     */
    private String getTempUserId(ServerHttpRequest request) {
        // 从请求头中获取临时用户id
        HttpHeaders headers = request.getHeaders();
        String userTempId = null;
        if (headers != null) {
            userTempId = headers.getFirst("userTempId");
        }
        // 如果请求头中的临时id为空，则从cookie中获取临时用户id
        if (userTempId == null) {
            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
            if (!CollectionUtils.isEmpty(cookies)) {
                HttpCookie cookie = cookies.getFirst("userTempId");
                if (cookie != null) {
                    userTempId = cookie.getValue();
                }
            }
        }
        return userTempId;
    }

    /**
     * 重定向到登录页面
     * @param request
     * @param response
     */
    private static Mono<Void> redirectLogin(ServerHttpRequest request, ServerHttpResponse response) {
        // 303状态码表示由于请求对应的资源存在着另一个URI，应使用重定向获取请求的资源
        response.setStatusCode(HttpStatus.FOUND);
        // 设置请求头信息，将请求重定向到登录页面
        response.getHeaders().set(HttpHeaders.LOCATION, "http://www.gmall.com/login.html?originUrl=" + request.getURI());
        // 设置响应完成
        return response.setComplete();
    }

    /**
     * 获取用户ID
     * 1、从请求头中获取token
     * 2、从cookie中获取token
     * 3、从redis中获取用户信息
     * 4、判断用户的ip地址与redis中的是否一致，防止用户被盗用
     * 5、返回用户ID
     *
     * @param request
     * @return
     */
    @SneakyThrows
    private String getUserId(ServerHttpRequest request) {
        // 从请求头中获取token
        List<String> tokens = request.getHeaders().get("token");
        String token = "";
        if (!CollectionUtils.isEmpty(tokens)) {
            token = tokens.get(0);
        } else {
            // 从cookie中获取token
            HttpCookie cookie = request.getCookies().getFirst("token");
            if (cookie != null) {
                token = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8.name());
            }
        }
        if (StringUtils.isNotBlank(token)) {
            // 从redis中获取用户信息
            String userJson = (String) redisTemplate.opsForValue().get(LOGIN_PREFIX + token);
            if (StringUtils.isNotBlank(userJson)) {
                // 需要判断用户的ip地址与redis中的是否一致，防止用户被盗用
                JSONObject userObj = JSONObject.parseObject(userJson);
                String ipAddress = userObj.getString("ipAddress");
                String currentIpAddress = IpUtil.getGatwayIpAddress(request);
                if (!currentIpAddress.equals(ipAddress)) {
                    // ip地址不一致，token被盗用
                    return "-1";
                } else {
                    return userObj.getString("userId");
                }
            }
        }
        return "";
    }

    /**
     * 响应结果
     * 1、内部接口拒绝访问
     * 2、token被盗用，拒绝访问
     * 3、路径中有auth，需要进行登录才能访问，返回用户没有权限登录
     * @param response
     * @param resultCodeEnum
     * @return
     */
    private Mono<Void> outResult(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        // 返回用户没有权限登录
        Result<Object> result = Result.build(null, resultCodeEnum);
        // 对象转为字节数组
        byte[] resultBytes = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        // 获取数据流工厂
        DataBuffer dataBuffer = response.bufferFactory().wrap(resultBytes);
        // 设置响应头上下文类型，前端可根据此类型进行相应处理
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(dataBuffer));
    }

    /**
     * 设置过滤器的执行顺序
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 1;
    }
}

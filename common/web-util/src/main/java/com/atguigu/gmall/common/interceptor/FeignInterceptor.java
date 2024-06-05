package com.atguigu.gmall.common.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * feign拦截器，解决服务之间调用时，请求头没有携带信息的问题
 */
@Component
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            String userId = request.getHeader("userId");
            if (StringUtils.isNotBlank(userId)) {
                requestTemplate.header("userId", userId);
            }
            String userTempId = request.getHeader("userTempId");
            if (StringUtils.isNotBlank(userTempId)) {
                requestTemplate.header("userTempId", userTempId);
            }
        }
    }
}

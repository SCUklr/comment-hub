package com.ssp.comment.interceptor;

import com.ssp.comment.common.LoginRequired;
import com.ssp.comment.common.Result;
import com.ssp.comment.common.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 模拟从Header或Cookie中获取用户ID，实际可接JWT/Session
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                UserContext.setCurrentUserId(Integer.valueOf(userIdStr));
            } catch (NumberFormatException ignored) {
            }
        }

        if (handler instanceof HandlerMethod handlerMethod) {
            LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);
            if (loginRequired != null && UserContext.getCurrentUserId() == null) {
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(Result.fail(401, "请先登录")));
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}

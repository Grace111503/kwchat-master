package com.kwp.chat.api.interceptor;

import com.kwp.chat.common.constant.CommonConstant;
import com.kwp.chat.common.utils.JwtUtils;
import com.kwp.chat.common.utils.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 获取Token
        String authHeader = request.getHeader(CommonConstant.TOKEN_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(CommonConstant.TOKEN_PREFIX)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":1001,\"message\":\"未登录或Token已过期\"}");
            return false;
        }

        String token = authHeader.substring(CommonConstant.TOKEN_PREFIX.length());

        try {
            // 验证Token
            if (!jwtUtils.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":1003,\"message\":\"Token无效\"}");
                return false;
            }

            // 获取用户信息
            Long userId = jwtUtils.getUserId(token);
            String username = jwtUtils.getUsername(token);

            // 检查Redis中的Token是否匹配
            String cachedToken = (String) redisUtils.get(CommonConstant.REDIS_TOKEN_PREFIX + userId);
            if (cachedToken == null || !cachedToken.equals(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":1004,\"message\":\"Token已过期\"}");
                return false;
            }

            // 将用户信息放入请求属性
            request.setAttribute(CommonConstant.USER_ID_ATTRIBUTE, userId);
            request.setAttribute(CommonConstant.USERNAME_ATTRIBUTE, username);

            return true;
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":1003,\"message\":\"Token无效\"}");
            return false;
        }
    }
}
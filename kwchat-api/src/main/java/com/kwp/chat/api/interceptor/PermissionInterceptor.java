package com.kwp.chat.api.interceptor;

import com.kwp.chat.common.annotation.RequiresPermission;
import com.kwp.chat.common.annotation.RequiresRole;
import com.kwp.chat.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 权限拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 只处理Controller方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 获取用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return true;
        }

        // 检查角色注解
        RequiresRole requiresRole = handlerMethod.getMethodAnnotation(RequiresRole.class);
        if (requiresRole != null) {
            if (!checkRole(userId, requiresRole)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":1002,\"message\":\"没有权限\"}");
                return false;
            }
        }

        // 检查权限注解
        RequiresPermission requiresPermission = handlerMethod.getMethodAnnotation(RequiresPermission.class);
        if (requiresPermission != null) {
            if (!checkPermission(userId, requiresPermission)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":1002,\"message\":\"没有权限\"}");
                return false;
            }
        }

        return true;
    }

    /**
     * 检查角色
     */
    private boolean checkRole(Long userId, RequiresRole annotation) {
        String[] roles = annotation.value();
        RequiresRole.Logical logical = annotation.logical();

        if (logical == RequiresRole.Logical.AND) {
            // 必须拥有所有角色
            for (String role : roles) {
                if (!authService.hasRole(userId, role)) {
                    return false;
                }
            }
            return true;
        } else {
            // 拥有任意一个角色即可
            return authService.hasAnyRole(userId, roles);
        }
    }

    /**
     * 检查权限
     */
    private boolean checkPermission(Long userId, RequiresPermission annotation) {
        String permission = annotation.value();
        RequiresPermission.Logical logical = annotation.logical();

        // 这里简化处理，实际可以根据logical判断AND/OR
        return authService.hasPermission(userId, permission);
    }
}
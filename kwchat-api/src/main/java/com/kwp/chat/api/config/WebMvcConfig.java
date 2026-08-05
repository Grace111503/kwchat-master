package com.kwp.chat.api.config;

import com.kwp.chat.api.interceptor.AuthInterceptor;
import com.kwp.chat.api.interceptor.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final PermissionInterceptor permissionInterceptor;

    @Value("${file.storage.local.path:D:/KuaiTong/kwchat/uploads}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 认证拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/user/**", "/friend/**", "/conversation/**", "/message/**", "/file/**", "/ai/**", "/auth/**", "/admin/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/refresh",
                        "/file/avatar/**",
                        "/file/image/**",
                        "/file/video/**",
                        "/file/voice/**",
                        "/file/document/**",
                        "/uploads/**",  // 静态资源不拦截
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**"
                );

        // 权限拦截器（在认证拦截器之后执行）
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/admin/**", "/auth/**")
                .order(1);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");

        // 映射本地文件存储目录
        // 确保路径以 / 结尾
        String path = uploadPath;
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        // Windows路径需要转换为URI格式 (file:///D:/path/)
        path = path.replace("\\", "/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + path);
    }
}
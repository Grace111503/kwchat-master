package com.kwp.chat.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 本地文件存储工具类
 * 用于替代MinIO，将文件存储到服务器本地磁盘
 */
@Slf4j
@Component
public class LocalStorageUtils {

    @Value("${file.storage.local.path:/opt/kwchat/uploads}")
    private String storagePath;

    @Value("${file.storage.local.url-prefix:/uploads}")
    private String urlPrefix;

    @Value("${server.port:8080}")
    private int serverPort;

    @PostConstruct
    public void init() {
        try {
            // 确保存储目录存在
            Path storageDir = Paths.get(storagePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
                log.info("创建本地存储目录: {}", storagePath);
            }
        } catch (IOException e) {
            log.error("初始化本地存储目录失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 上传文件
     *
     * @param inputStream 文件输入流
     * @param objectName  文件路径（如：avatar/2024/01/xxx.jpg）
     * @param contentType 文件类型
     * @param size        文件大小
     * @return 文件访问URL（相对路径，由Nginx代理访问）
     */
    public String uploadFile(InputStream inputStream, String objectName, String contentType, long size) {
        try {
            // 构建完整的文件路径
            Path filePath = Paths.get(storagePath, objectName);

            // 创建父目录
            Path parentDir = filePath.getParent();
            if (!Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // 写入文件
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回相对路径，前端通过Nginx访问
            String url = urlPrefix + "/" + objectName;
            log.info("文件上传成功: objectName={}, size={}, url={}", objectName, size, url);
            return url;
        } catch (IOException e) {
            log.error("文件上传失败: objectName={}, error={}", objectName, e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件访问URL（相对路径）
     */
    public String getFileUrl(String objectName) {
        return urlPrefix + "/" + objectName;
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        try {
            Path filePath = Paths.get(storagePath, objectName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("文件删除成功: objectName={}", objectName);
            }
        } catch (IOException e) {
            log.error("文件删除失败: objectName={}, error={}", objectName, e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String objectName) {
        try {
            Path filePath = Paths.get(storagePath, objectName);
            if (Files.exists(filePath)) {
                return Files.newInputStream(filePath);
            }
            log.warn("文件不存在: objectName={}", objectName);
            return null;
        } catch (IOException e) {
            log.error("文件下载失败: objectName={}, error={}", objectName, e.getMessage());
            return null;
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String objectName) {
        Path filePath = Paths.get(storagePath, objectName);
        return Files.exists(filePath);
    }
}

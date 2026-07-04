package com.kwp.chat.common.utils;

import com.kwp.chat.common.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtils {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 检查存储桶是否存在
     */
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("检查存储桶是否存在失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 创建存储桶
     */
    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }
        } catch (Exception e) {
            log.error("创建存储桶失败: {}", e.getMessage());
        }
    }

    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            createBucket(minioConfig.getBucketName());
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return getPresignedUrl(objectName);
        } catch (Exception e) {
            log.error("上传文件失败: {}", e.getMessage());
            throw new RuntimeException("上传文件失败");
        }
    }

    /**
     * 上传文件（带自定义ContentType）
     */
    public String uploadFile(InputStream inputStream, String objectName, String contentType, long size) {
        try {
            createBucket(minioConfig.getBucketName());
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            return getPresignedUrl(objectName);
        } catch (Exception e) {
            log.error("上传文件失败: {}", e.getMessage());
            throw new RuntimeException("上传文件失败");
        }
    }

    /**
     * 获取文件访问URL
     */
    public String getFileUrl(String objectName) {
        return minioConfig.getUrl() + "/" + minioConfig.getBucketName() + "/" + objectName;
    }

    /**
     * 获取预签名URL
     */
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .expiry(7, TimeUnit.DAYS)
                    .build());
        } catch (Exception e) {
            log.error("获取预签名URL失败: {}", e.getMessage());
            return getFileUrl(objectName);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage());
        }
    }

    /**
     * 获取文件信息
     */
    public StatObjectResponse getFileInfo(String objectName) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage());
            return null;
        }
    }
}
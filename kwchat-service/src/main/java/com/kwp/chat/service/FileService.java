package com.kwp.chat.service;

import com.kwp.chat.model.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     */
    FileUploadResponse uploadFile(MultipartFile file, String directory);

    /**
     * 上传图片
     */
    FileUploadResponse uploadImage(MultipartFile file);

    /**
     * 上传视频
     */
    FileUploadResponse uploadVideo(MultipartFile file);

    /**
     * 上传语音
     */
    FileUploadResponse uploadVoice(MultipartFile file);

    /**
     * 上传头像
     */
    FileUploadResponse uploadAvatar(MultipartFile file);

    /**
     * 上传聊天文件
     */
    FileUploadResponse uploadChatFile(MultipartFile file);

    /**
     * 删除文件
     */
    void deleteFile(String filePath);

    /**
     * 获取文件访问URL
     */
    String getFileUrl(String filePath);

    /**
     * 获取预签名URL
     */
    String getPresignedUrl(String filePath);

    /**
     * 下载文件
     */
    InputStream downloadFile(String filePath);

    /**
     * 检查文件类型是否允许
     */
    boolean isAllowedFileType(String fileType, String category);

    /**
     * 检查文件大小是否超限
     */
    boolean isFileSizeValid(Long fileSize, String category);
}
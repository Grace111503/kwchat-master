package com.kwp.chat.service.impl;

import com.kwp.chat.common.config.MinioConfig;
import com.kwp.chat.common.constant.CommonConstant;
import com.kwp.chat.common.exception.BusinessException;
import com.kwp.chat.common.result.ResultCode;
import com.kwp.chat.common.utils.MinioUtils;
import com.kwp.chat.model.dto.FileUploadResponse;
import com.kwp.chat.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioUtils minioUtils;
    private final MinioConfig minioConfig;

    /**
     * 允许的图片类型
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    /**
     * 允许的视频类型
     */
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/avi", "video/quicktime", "video/x-ms-wmv", "video/webm"
    );

    /**
     * 允许的语音类型
     */
    private static final List<String> ALLOWED_VOICE_TYPES = Arrays.asList(
            "audio/mpeg", "audio/wav", "audio/ogg", "audio/aac", "audio/amr"
    );

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String directory) {
        // 验证文件
        validateFile(file);

        try {
            // 生成唯一文件名
            String originalFileName = file.getOriginalFilename();
            String extension = getFileExtension(originalFileName);
            String fileName = generateFileName(extension);

            // 构建文件路径
            String filePath = directory + fileName;

            // 上传到MinIO
            String fileUrl = minioUtils.uploadFile(file.getInputStream(), filePath, file.getContentType(), file.getSize());

            log.info("文件上传成功: fileName={}, size={}, url={}", originalFileName, file.getSize(), fileUrl);

            return FileUploadResponse.builder()
                    .url(fileUrl)
                    .fileName(fileName)
                    .originalFileName(originalFileName)
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .filePath(filePath)
                    .build();
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public FileUploadResponse uploadImage(MultipartFile file) {
        // 验证图片类型
        if (!isAllowedFileType(file.getContentType(), "image")) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED, "不支持的图片格式");
        }

        // 验证图片大小
        if (!isFileSizeValid(file.getSize(), "image")) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED, "图片大小不能超过10MB");
        }

        return uploadFile(file, CommonConstant.FILE_PATH_IMAGE);
    }

    @Override
    public FileUploadResponse uploadVideo(MultipartFile file) {
        // 验证视频类型
        if (!isAllowedFileType(file.getContentType(), "video")) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED, "不支持的视频格式");
        }

        // 验证视频大小
        if (!isFileSizeValid(file.getSize(), "video")) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED, "视频大小不能超过50MB");
        }

        return uploadFile(file, CommonConstant.FILE_PATH_VIDEO);
    }

    @Override
    public FileUploadResponse uploadVoice(MultipartFile file) {
        // 验证语音类型
        if (!isAllowedFileType(file.getContentType(), "voice")) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED, "不支持的语音格式");
        }

        // 验证语音大小
        if (!isFileSizeValid(file.getSize(), "voice")) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED, "语音大小不能超过5MB");
        }

        return uploadFile(file, CommonConstant.FILE_PATH_VOICE);
    }

    @Override
    public FileUploadResponse uploadAvatar(MultipartFile file) {
        // 验证图片类型
        if (!isAllowedFileType(file.getContentType(), "image")) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORTED, "不支持的图片格式");
        }

        // 验证图片大小
        if (!isFileSizeValid(file.getSize(), "image")) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED, "头像大小不能超过10MB");
        }

        return uploadFile(file, CommonConstant.FILE_PATH_AVATAR);
    }

    @Override
    public FileUploadResponse uploadChatFile(MultipartFile file) {
        // 验证文件大小
        if (!isFileSizeValid(file.getSize(), "file")) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED, "文件大小不能超过100MB");
        }

        return uploadFile(file, CommonConstant.FILE_PATH_FILE);
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            minioUtils.deleteFile(filePath);
            log.info("文件删除成功: filePath={}", filePath);
        } catch (Exception e) {
            log.error("文件删除失败: filePath={}, error={}", filePath, e.getMessage());
            throw new BusinessException(ResultCode.FILE_UPLOAD_FAILED, "文件删除失败");
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        return minioUtils.getFileUrl(filePath);
    }

    @Override
    public String getPresignedUrl(String filePath) {
        return minioUtils.getPresignedUrl(filePath);
    }

    @Override
    public InputStream downloadFile(String filePath) {
        return minioUtils.downloadFile(filePath);
    }

    @Override
    public boolean isAllowedFileType(String fileType, String category) {
        if (!StringUtils.hasText(fileType)) {
            return false;
        }

        return switch (category) {
            case "image" -> ALLOWED_IMAGE_TYPES.contains(fileType.toLowerCase());
            case "video" -> ALLOWED_VIDEO_TYPES.contains(fileType.toLowerCase());
            case "voice" -> ALLOWED_VOICE_TYPES.contains(fileType.toLowerCase());
            case "file" -> true; // 文件类型不限制
            default -> false;
        };
    }

    @Override
    public boolean isFileSizeValid(Long fileSize, String category) {
        if (fileSize == null || fileSize <= 0) {
            return false;
        }

        return switch (category) {
            case "image" -> fileSize <= CommonConstant.MAX_IMAGE_SIZE;
            case "video" -> fileSize <= CommonConstant.MAX_VIDEO_SIZE;
            case "voice" -> fileSize <= CommonConstant.MAX_VOICE_SIZE;
            case "file" -> fileSize <= CommonConstant.MAX_FILE_SIZE;
            default -> false;
        };
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件不能为空");
        }

        String originalFileName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFileName)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件名不能为空");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
}
package com.kwp.chat.api.controller;

import com.kwp.chat.common.config.MinioConfig;
import com.kwp.chat.common.result.Result;
import com.kwp.chat.common.utils.MinioUtils;
import com.kwp.chat.model.dto.FileUploadResponse;
import com.kwp.chat.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 文件控制器
 */
@Tag(name = "文件管理", description = "文件上传、下载等接口")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final MinioUtils minioUtils;
    private final MinioConfig minioConfig;

    @Value("${file.storage.local.path:D:/KuaiTong/kwchat/uploads}")
    private String uploadPath;

    @Operation(summary = "上传图片")
    @PostMapping("/image")
    public Result<FileUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileService.uploadImage(file);
        return Result.success(response);
    }

    @Operation(summary = "上传视频")
    @PostMapping("/video")
    public Result<FileUploadResponse> uploadVideo(@RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileService.uploadVideo(file);
        return Result.success(response);
    }

    @Operation(summary = "上传语音")
    @PostMapping("/voice")
    public Result<FileUploadResponse> uploadVoice(@RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileService.uploadVoice(file);
        return Result.success(response);
    }

    @Operation(summary = "上传文件")
    @PostMapping("/document")
    public Result<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        FileUploadResponse response = fileService.uploadChatFile(file);
        return Result.success(response);
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<FileUploadResponse> uploadAvatar(HttpServletRequest request,
                                                    @RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId(request);
        FileUploadResponse response = fileService.uploadAvatar(file);
        return Result.success(response);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping
    public Result<Void> deleteFile(@RequestParam("filePath") String filePath) {
        fileService.deleteFile(filePath);
        return Result.success();
    }

    @Operation(summary = "获取文件访问URL")
    @GetMapping("/url")
    public Result<String> getFileUrl(@RequestParam("filePath") String filePath) {
        String url = fileService.getFileUrl(filePath);
        return Result.success(url);
    }

    @Operation(summary = "获取预签名URL")
    @GetMapping("/presigned-url")
    public Result<String> getPresignedUrl(@RequestParam("filePath") String filePath) {
        String url = fileService.getPresignedUrl(filePath);
        return Result.success(url);
    }

    @Operation(summary = "获取头像文件")
    @GetMapping("/avatar/{fileName}")
    public void getAvatar(@PathVariable String fileName, HttpServletResponse response) {
        serveFile(Paths.get(uploadPath, "avatar", fileName).toFile(), "image/jpeg", response);
    }

    @Operation(summary = "获取图片文件")
    @GetMapping("/image/{fileName}")
    public void getImage(@PathVariable String fileName, HttpServletResponse response) {
        File file = Paths.get(uploadPath, "image", fileName).toFile();
        serveFile(file, getContentType(fileName), response);
    }

    @Operation(summary = "获取图片文件（支持完整路径）")
    @GetMapping("/image/path/{filePath:.+}")
    public void getImageByPath(@PathVariable String filePath, HttpServletResponse response) {
        File file = Paths.get(uploadPath, "image", filePath).toFile();
        serveFile(file, getContentType(filePath), response);
    }

    @Operation(summary = "获取视频文件")
    @GetMapping("/video/{fileName}")
    public void getVideo(@PathVariable String fileName, HttpServletResponse response) {
        File file = Paths.get(uploadPath, "video", fileName).toFile();
        serveFile(file, getContentType(fileName), response);
    }

    @Operation(summary = "获取视频文件（支持完整路径）")
    @GetMapping("/video/path/{filePath:.+}")
    public void getVideoByPath(@PathVariable String filePath, HttpServletResponse response) {
        File file = Paths.get(uploadPath, "video", filePath).toFile();
        serveFile(file, getContentType(filePath), response);
    }

    @Operation(summary = "获取语音文件")
    @GetMapping("/voice/{fileName}")
    public void getVoice(@PathVariable String fileName, HttpServletResponse response) {
        File file = Paths.get(uploadPath, "voice", fileName).toFile();
        serveFile(file, getContentType(fileName), response);
    }

    @Operation(summary = "获取语音文件（支持完整路径）")
    @GetMapping("/voice/path/{filePath:.+}")
    public void getVoiceByPath(@PathVariable String filePath, HttpServletResponse response) {
        File file = Paths.get(uploadPath, "voice", filePath).toFile();
        serveFile(file, getContentType(filePath), response);
    }

    @Operation(summary = "获取文档文件")
    @GetMapping("/document/{fileName}")
    public void getDocument(@PathVariable String fileName, HttpServletResponse response) {
        File file = Paths.get(uploadPath, "file", fileName).toFile();
        serveFile(file, getContentType(fileName), response);
    }

    @Operation(summary = "获取文档文件（支持完整路径）")
    @GetMapping("/document/path/{filePath:.+}")
    public void getDocumentByPath(@PathVariable String filePath, HttpServletResponse response) {
        File file = Paths.get(uploadPath, "file", filePath).toFile();
        serveFile(file, getContentType(filePath), response);
    }

    /**
     * 通用文件服务方法
     */
    private void serveFile(File file, String contentType, HttpServletResponse response) {
        try {
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 设置响应头
            response.setContentType(contentType);
            response.setContentLength((int) file.length());
            response.setHeader("Cache-Control", "max-age=31536000");
            response.setHeader("Access-Control-Allow-Origin", "*");

            // 写入响应
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据文件扩展名获取Content-Type
     */
    private String getContentType(String fileName) {
        String extension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        }

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "aac" -> "audio/aac";
            case "m4a" -> "audio/mp4";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    @Operation(summary = "检查MinIO连接状态")
    @GetMapping("/minio-status")
    public Result<Map<String, Object>> checkMinioStatus() {
        Map<String, Object> status = new java.util.HashMap<>();
        try {
            // 检查bucket是否存在
            boolean bucketExists = minioUtils.bucketExists(minioConfig.getBucketName());
            status.put("bucketExists", bucketExists);
            status.put("endpoint", minioConfig.getEndpoint());
            status.put("bucketName", minioConfig.getBucketName());
            status.put("url", minioConfig.getUrl());
            status.put("status", "connected");
            return Result.success(status);
        } catch (Exception e) {
            status.put("status", "error");
            status.put("error", e.getMessage());
            status.put("endpoint", minioConfig.getEndpoint());
            return Result.success(status);
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        return userId;
    }
}
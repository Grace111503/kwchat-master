package com.kwp.chat.api.controller;

import com.kwp.chat.common.result.Result;
import com.kwp.chat.model.dto.admin.AiModelConfigDTO;
import com.kwp.chat.model.dto.admin.DashboardStats;
import com.kwp.chat.model.dto.admin.SystemConfigDTO;
import com.kwp.chat.model.system.AiModelConfig;
import com.kwp.chat.model.system.LoginLog;
import com.kwp.chat.model.system.OperationLog;
import com.kwp.chat.model.system.SystemConfig;
import com.kwp.chat.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台管理控制器
 */
@Tag(name = "后台管理", description = "系统配置、日志管理、AI模型配置等")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ========== 仪表盘 ==========

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard")
    public Result<DashboardStats> getDashboardStats() {
        DashboardStats stats = adminService.getDashboardStats();
        return Result.success(stats);
    }

    // ========== 系统配置 ==========

    @Operation(summary = "获取所有系统配置")
    @GetMapping("/config")
    public Result<List<SystemConfig>> getAllConfigs() {
        List<SystemConfig> configs = adminService.getAllConfigs();
        return Result.success(configs);
    }

    @Operation(summary = "根据分组获取配置")
    @GetMapping("/config/group/{group}")
    public Result<List<SystemConfig>> getConfigsByGroup(@PathVariable String group) {
        List<SystemConfig> configs = adminService.getConfigsByGroup(group);
        return Result.success(configs);
    }

    @Operation(summary = "获取配置值")
    @GetMapping("/config/{key}")
    public Result<String> getConfigValue(@PathVariable String key) {
        String value = adminService.getConfigValue(key);
        return Result.success(value);
    }

    @Operation(summary = "保存系统配置")
    @PostMapping("/config")
    public Result<Void> saveConfig(@Valid @RequestBody SystemConfigDTO configDTO) {
        adminService.saveConfig(configDTO);
        return Result.success();
    }

    @Operation(summary = "更新系统配置")
    @PutMapping("/config/{key}")
    public Result<Void> updateConfig(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        adminService.updateConfig(key, value);
        return Result.success();
    }

    @Operation(summary = "删除系统配置")
    @DeleteMapping("/config/{key}")
    public Result<Void> deleteConfig(@PathVariable String key) {
        adminService.deleteConfig(key);
        return Result.success();
    }

    // ========== 操作日志 ==========

    @Operation(summary = "获取操作日志")
    @GetMapping("/log/operation")
    public Result<List<OperationLog>> getOperationLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<OperationLog> logs = adminService.getOperationLogs(page, size);
        return Result.success(logs);
    }

    @Operation(summary = "清空操作日志")
    @DeleteMapping("/log/operation")
    public Result<Void> clearOperationLogs() {
        adminService.clearOperationLogs();
        return Result.success();
    }

    // ========== 登录日志 ==========

    @Operation(summary = "获取登录日志")
    @GetMapping("/log/login")
    public Result<List<LoginLog>> getLoginLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<LoginLog> logs = adminService.getLoginLogs(page, size);
        return Result.success(logs);
    }

    @Operation(summary = "清空登录日志")
    @DeleteMapping("/log/login")
    public Result<Void> clearLoginLogs() {
        adminService.clearLoginLogs();
        return Result.success();
    }

    // ========== AI模型配置 ==========

    @Operation(summary = "获取所有AI模型配置")
    @GetMapping("/ai-model")
    public Result<List<AiModelConfig>> getAllAiModelConfigs() {
        List<AiModelConfig> configs = adminService.getAllAiModelConfigs();
        return Result.success(configs);
    }

    @Operation(summary = "获取启用的AI模型")
    @GetMapping("/ai-model/enabled")
    public Result<List<AiModelConfig>> getEnabledAiModelConfigs() {
        List<AiModelConfig> configs = adminService.getEnabledAiModelConfigs();
        return Result.success(configs);
    }

    @Operation(summary = "获取默认AI模型")
    @GetMapping("/ai-model/default")
    public Result<AiModelConfig> getDefaultAiModel() {
        AiModelConfig config = adminService.getDefaultAiModel();
        return Result.success(config);
    }

    @Operation(summary = "保存AI模型配置")
    @PostMapping("/ai-model")
    public Result<Void> saveAiModelConfig(@Valid @RequestBody AiModelConfigDTO configDTO) {
        adminService.saveAiModelConfig(configDTO);
        return Result.success();
    }

    @Operation(summary = "更新AI模型配置")
    @PutMapping("/ai-model/{id}")
    public Result<Void> updateAiModelConfig(@PathVariable Long id, @Valid @RequestBody AiModelConfigDTO configDTO) {
        adminService.updateAiModelConfig(id, configDTO);
        return Result.success();
    }

    @Operation(summary = "删除AI模型配置")
    @DeleteMapping("/ai-model/{id}")
    public Result<Void> deleteAiModelConfig(@PathVariable Long id) {
        adminService.deleteAiModelConfig(id);
        return Result.success();
    }

    @Operation(summary = "设置默认AI模型")
    @PutMapping("/ai-model/{id}/default")
    public Result<Void> setDefaultAiModel(@PathVariable Long id) {
        adminService.setDefaultAiModel(id);
        return Result.success();
    }

    @Operation(summary = "测试AI模型连接")
    @PostMapping("/ai-model/{id}/test")
    public Result<Boolean> testAiModelConnection(@PathVariable Long id) {
        boolean result = adminService.testAiModelConnection(id);
        return Result.success(result);
    }
}
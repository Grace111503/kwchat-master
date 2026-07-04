package com.kwp.chat.service;

import com.kwp.chat.model.dto.admin.AiModelConfigDTO;
import com.kwp.chat.model.dto.admin.DashboardStats;
import com.kwp.chat.model.dto.admin.SystemConfigDTO;
import com.kwp.chat.model.system.AiModelConfig;
import com.kwp.chat.model.system.LoginLog;
import com.kwp.chat.model.system.OperationLog;
import com.kwp.chat.model.system.SystemConfig;

import java.util.List;

/**
 * 后台管理服务接口
 */
public interface AdminService {

    // ========== 仪表盘 ==========

    /**
     * 获取仪表盘统计数据
     */
    DashboardStats getDashboardStats();

    // ========== 系统配置 ==========

    /**
     * 获取所有配置
     */
    List<SystemConfig> getAllConfigs();

    /**
     * 根据分组获取配置
     */
    List<SystemConfig> getConfigsByGroup(String group);

    /**
     * 获取配置值
     */
    String getConfigValue(String key);

    /**
     * 保存配置
     */
    void saveConfig(SystemConfigDTO configDTO);

    /**
     * 更新配置
     */
    void updateConfig(String key, String value);

    /**
     * 删除配置
     */
    void deleteConfig(String key);

    // ========== 操作日志 ==========

    /**
     * 获取操作日志列表
     */
    List<OperationLog> getOperationLogs(int page, int size);

    /**
     * 清空操作日志
     */
    void clearOperationLogs();

    // ========== 登录日志 ==========

    /**
     * 获取登录日志列表
     */
    List<LoginLog> getLoginLogs(int page, int size);

    /**
     * 清空登录日志
     */
    void clearLoginLogs();

    // ========== AI模型配置 ==========

    /**
     * 获取所有AI模型配置
     */
    List<AiModelConfig> getAllAiModelConfigs();

    /**
     * 获取启用的AI模型配置
     */
    List<AiModelConfig> getEnabledAiModelConfigs();

    /**
     * 获取默认AI模型
     */
    AiModelConfig getDefaultAiModel();

    /**
     * 保存AI模型配置
     */
    void saveAiModelConfig(AiModelConfigDTO configDTO);

    /**
     * 更新AI模型配置
     */
    void updateAiModelConfig(Long id, AiModelConfigDTO configDTO);

    /**
     * 删除AI模型配置
     */
    void deleteAiModelConfig(Long id);

    /**
     * 设置默认AI模型
     */
    void setDefaultAiModel(Long id);

    /**
     * 测试AI模型连接
     */
    boolean testAiModelConnection(Long id);
}
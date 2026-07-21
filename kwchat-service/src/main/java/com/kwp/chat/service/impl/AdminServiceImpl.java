package com.kwp.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kwp.chat.common.exception.BusinessException;
import com.kwp.chat.common.result.ResultCode;
import com.kwp.chat.dao.*;
import com.kwp.chat.model.dto.admin.AiModelConfigDTO;
import com.kwp.chat.model.dto.admin.DashboardStats;
import com.kwp.chat.model.dto.admin.SystemConfigDTO;
import com.kwp.chat.model.message.Message;
import com.kwp.chat.model.system.AiModelConfig;
import com.kwp.chat.model.system.LoginLog;
import com.kwp.chat.model.system.OperationLog;
import com.kwp.chat.model.system.SystemConfig;
import com.kwp.chat.model.user.User;
import com.kwp.chat.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 后台管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final MessageMapper messageMapper;
    private final ConversationMapper conversationMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final OperationLogMapper operationLogMapper;
    private final LoginLogMapper loginLogMapper;
    private final AiModelConfigMapper aiModelConfigMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AiClientFactory aiClientFactory;

    @Override
    public DashboardStats getDashboardStats() {
        // 用户统计
        Long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getDeleted, 0));
        Long todayNewUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
                .eq(User::getDeleted, 0));

        // 在线用户数（从Redis获取）
        Long onlineUsers = getOnlineUserCount();

        // 消息统计
        Long totalMessages = messageMapper.selectCount(new LambdaQueryWrapper<Message>().eq(Message::getDeleted, 0));
        Long todayMessages = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .ge(Message::getCreateTime, LocalDateTime.of(LocalDate.now(), LocalTime.MIN))
                .eq(Message::getDeleted, 0));

        // 会话统计
        Long totalConversations = conversationMapper.selectCount(null);

        // 近7天用户注册趋势
        List<Map<String, Object>> userTrend = getUserTrend(7);

        // 近7天消息趋势
        List<Map<String, Object>> messageTrend = getMessageTrend(7);

        // 消息类型分布
        Map<String, Long> messageTypeDistribution = getMessageTypeDistribution();

        return DashboardStats.builder()
                .totalUsers(totalUsers)
                .todayNewUsers(todayNewUsers)
                .onlineUsers(onlineUsers)
                .totalMessages(totalMessages)
                .todayMessages(todayMessages)
                .totalConversations(totalConversations)
                .totalFiles(0L)
                .storageUsed(0L)
                .userTrend(userTrend)
                .messageTrend(messageTrend)
                .messageTypeDistribution(messageTypeDistribution)
                .aiCallCount(0L)
                .aiTokenUsed(0L)
                .build();
    }

    @Override
    public List<SystemConfig> getAllConfigs() {
        return systemConfigMapper.selectAll();
    }

    @Override
    public List<SystemConfig> getConfigsByGroup(String group) {
        return systemConfigMapper.selectByGroup(group);
    }

    @Override
    public String getConfigValue(String key) {
        SystemConfig config = systemConfigMapper.selectByConfigKey(key);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(SystemConfigDTO configDTO) {
        SystemConfig existing = systemConfigMapper.selectByConfigKey(configDTO.getConfigKey());
        if (existing != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "配置键已存在");
        }

        SystemConfig config = new SystemConfig();
        BeanUtils.copyProperties(configDTO, config);
        config.setIsSystem(0);
        systemConfigMapper.insert(config);

        log.info("保存系统配置: key={}", configDTO.getConfigKey());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(String key, String value) {
        SystemConfig config = systemConfigMapper.selectByConfigKey(key);
        if (config == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "配置不存在");
        }

        config.setConfigValue(value);
        systemConfigMapper.updateById(config);

        log.info("更新系统配置: key={}, value={}", key, value);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(String key) {
        SystemConfig config = systemConfigMapper.selectByConfigKey(key);
        if (config == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "配置不存在");
        }

        if (config.getIsSystem() == 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "系统内置配置不能删除");
        }

        systemConfigMapper.deleteById(config.getId());

        log.info("删除系统配置: key={}", key);
    }

    @Override
    public List<OperationLog> getOperationLogs(int page, int size) {
        Page<OperationLog> pageParam = new Page<>(page, size);
        return operationLogMapper.selectPage(pageParam,
                new LambdaQueryWrapper<OperationLog>()
                        .eq(OperationLog::getDeleted, 0)
                        .orderByDesc(OperationLog::getCreateTime))
                .getRecords();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearOperationLogs() {
        // 逻辑删除所有操作日志
        operationLogMapper.update(null, new LambdaUpdateWrapper<OperationLog>()
                .set(OperationLog::getDeleted, 1));
        log.info("清空操作日志");
    }

    @Override
    public List<LoginLog> getLoginLogs(int page, int size) {
        Page<LoginLog> pageParam = new Page<>(page, size);
        return loginLogMapper.selectPage(pageParam,
                new LambdaQueryWrapper<LoginLog>()
                        .eq(LoginLog::getDeleted, 0)
                        .orderByDesc(LoginLog::getCreateTime))
                .getRecords();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearLoginLogs() {
        loginLogMapper.update(null, new LambdaUpdateWrapper<LoginLog>()
                .set(LoginLog::getDeleted, 1));
        log.info("清空登录日志");
    }

    @Override
    public List<AiModelConfig> getAllAiModelConfigs() {
        return aiModelConfigMapper.selectList(
                new LambdaQueryWrapper<AiModelConfig>()
                        .eq(AiModelConfig::getDeleted, 0)
                        .orderByAsc(AiModelConfig::getSortOrder));
    }

    @Override
    public List<AiModelConfig> getEnabledAiModelConfigs() {
        return aiModelConfigMapper.selectEnabledModels();
    }

    @Override
    public AiModelConfig getDefaultAiModel() {
        return aiModelConfigMapper.selectDefaultModel();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAiModelConfig(AiModelConfigDTO configDTO) {
        AiModelConfig config = new AiModelConfig();
        BeanUtils.copyProperties(configDTO, config);
        aiModelConfigMapper.insert(config);

        log.info("保存AI模型配置: name={}", configDTO.getModelName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAiModelConfig(Long id, AiModelConfigDTO configDTO) {
        AiModelConfig config = aiModelConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "模型配置不存在");
        }

        BeanUtils.copyProperties(configDTO, config);
        aiModelConfigMapper.updateById(config);

        log.info("更新AI模型配置: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAiModelConfig(Long id) {
        AiModelConfig config = aiModelConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "模型配置不存在");
        }

        if (config.getIsDefault() == 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "默认模型不能删除");
        }

        aiModelConfigMapper.deleteById(id);

        log.info("删除AI模型配置: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAiModel(Long id) {
        // 取消所有默认
        aiModelConfigMapper.update(null, new LambdaUpdateWrapper<AiModelConfig>()
                .set(AiModelConfig::getIsDefault, 0));

        // 设置新的默认
        AiModelConfig config = aiModelConfigMapper.selectById(id);
        if (config != null) {
            config.setIsDefault(1);
            aiModelConfigMapper.updateById(config);
        }

        log.info("设置默认AI模型: id={}", id);
    }

    @Override
    public boolean testAiModelConnection(Long id) {
        AiModelConfig config = aiModelConfigMapper.selectById(id);
        if (config == null) {
            return false;
        }

        try {
            log.info("测试AI模型连接: model={}", config.getModelName());
            return aiClientFactory.testConnection(config);
        } catch (Exception e) {
            log.error("AI模型连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    // ========== 私有方法 ==========

    /**
     * 获取在线用户数
     */
    private Long getOnlineUserCount() {
        try {
            Set<String> keys = redisTemplate.keys("user:online:*");
            return keys != null ? (long) keys.size() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 获取用户注册趋势
     */
    private List<Map<String, Object>> getUserTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .between(User::getCreateTime, start, end)
                    .eq(User::getDeleted, 0));

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", count);
            trend.add(item);
        }

        return trend;
    }

    /**
     * 获取消息趋势
     */
    private List<Map<String, Object>> getMessageTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            Long count = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                    .between(Message::getCreateTime, start, end)
                    .eq(Message::getDeleted, 0));

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", count);
            trend.add(item);
        }

        return trend;
    }

    /**
     * 获取消息类型分布
     */
    private Map<String, Long> getDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("文本", 0L);
        distribution.put("图片", 0L);
        distribution.put("文件", 0L);
        distribution.put("视频", 0L);
        distribution.put("语音", 0L);
        return distribution;
    }

    private Map<String, Long> getMessageTypeDistribution() {
        return getDistribution();
    }
}
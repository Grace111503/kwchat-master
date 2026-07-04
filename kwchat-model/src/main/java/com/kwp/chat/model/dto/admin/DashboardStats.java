package com.kwp.chat.model.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘统计数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户总数
     */
    private Long totalUsers;

    /**
     * 今日新增用户
     */
    private Long todayNewUsers;

    /**
     * 在线用户数
     */
    private Long onlineUsers;

    /**
     * 消息总数
     */
    private Long totalMessages;

    /**
     * 今日消息数
     */
    private Long todayMessages;

    /**
     * 会话总数
     */
    private Long totalConversations;

    /**
     * 文件总数
     */
    private Long totalFiles;

    /**
     * 存储空间使用（字节）
     */
    private Long storageUsed;

    /**
     * 近7天用户注册趋势
     */
    private List<Map<String, Object>> userTrend;

    /**
     * 近7天消息趋势
     */
    private List<Map<String, Object>> messageTrend;

    /**
     * 消息类型分布
     */
    private Map<String, Long> messageTypeDistribution;

    /**
     * AI调用统计
     */
    private Long aiCallCount;

    /**
     * AI Token使用量
     */
    private Long aiTokenUsed;
}
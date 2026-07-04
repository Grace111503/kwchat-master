package com.kwp.chat.model.message;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kwp.chat.model.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息已读状态实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_message_read")
public class MessageRead extends BaseEntity {

    /**
     * 消息ID
     */
    @TableField("message_id")
    private Long messageId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 已读时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;
}
package com.kwp.chat.model.message;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kwp.chat.model.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户隐藏消息实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_message_hidden")
public class MessageHidden extends BaseEntity {

    /**
     * 消息ID
     */
    @TableField("message_id")
    private Long messageId;

    /**
     * 用户ID（谁隐藏了这条消息）
     */
    @TableField("user_id")
    private Long userId;
}

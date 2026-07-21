package com.kwp.chat.model.message;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kwp.chat.model.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息收藏实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_message_favorite")
public class MessageFavorite extends BaseEntity {

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
}

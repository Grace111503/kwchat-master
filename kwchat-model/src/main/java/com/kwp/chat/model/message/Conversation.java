package com.kwp.chat.model.message;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kwp.chat.model.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 会话实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_conversation")
public class Conversation extends BaseEntity {

    /**
     * 会话类型（1：单聊，2：群聊）
     */
    @TableField("conversation_type")
    private Integer conversationType;

    /**
     * 会话名称（群聊时有值）
     */
    @TableField("name")
    private String name;

    /**
     * 会话头像（群聊时有值）
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 最后一条消息ID
     */
    @TableField("last_message_id")
    private Long lastMessageId;

    /**
     * 最后一条消息内容
     */
    @TableField("last_message_content")
    private String lastMessageContent;

    /**
     * 最后消息时间
     */
    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;

    /**
     * 最后消息发送者ID
     */
    @TableField("last_message_sender_id")
    private Long lastMessageSenderId;

    /**
     * 成员数量
     */
    @TableField("member_count")
    private Integer memberCount;

    /**
     * 创建者ID
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 群公告
     */
    @TableField("announcement")
    private String announcement;

    /**
     * 免打扰（0：否，1：是）
     */
    @TableField("do_not_disturb")
    private Integer doNotDisturb;

    /**
     * 置顶（0：否，1：是）
     */
    @TableField("is_top")
    private Integer isTop;
}
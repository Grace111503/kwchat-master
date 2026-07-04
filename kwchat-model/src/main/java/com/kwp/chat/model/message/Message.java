package com.kwp.chat.model.message;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kwp.chat.model.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_message")
public class Message extends BaseEntity {

    /**
     * 会话ID
     */
    @TableField("conversation_id")
    private Long conversationId;

    /**
     * 发送者ID
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 消息类型（1：文本，2：图片，3：文件，4：视频，5：语音，6：系统，7：撤回）
     */
    @TableField("message_type")
    private Integer messageType;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 文件URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件类型（MIME类型）
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 缩略图URL
     */
    @TableField("thumbnail_url")
    private String thumbnailUrl;

    /**
     * 视频时长（秒）
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 回复的消息ID
     */
    @TableField("reply_message_id")
    private Long replyMessageId;

    /**
     * @的用户ID列表（JSON格式）
     */
    @TableField("at_user_ids")
    private String atUserIds;

    /**
     * 消息状态（0：正常，1：已撤回，2：已删除）
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否已读（0：未读，1：已读）
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 客户端消息ID（用于去重）
     */
    @TableField("client_message_id")
    private String clientMessageId;

    /**
     * 序列号（用于消息排序）
     */
    @TableField("sequence")
    private Long sequence;
}
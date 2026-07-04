package com.kwp.chat.model.message;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kwp.chat.model.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 会话成员实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_conversation_member")
public class ConversationMember extends BaseEntity {

    /**
     * 会话ID
     */
    @TableField("conversation_id")
    private Long conversationId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 成员昵称（群内昵称）
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 角色（0：普通成员，1：管理员，2：群主）
     */
    @TableField("role")
    private Integer role;

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

    /**
     * 最后读取时间
     */
    @TableField("last_read_time")
    private LocalDateTime lastReadTime;

    /**
     * 未读消息数
     */
    @TableField("unread_count")
    private Integer unreadCount;

    /**
     * 加入时间
     */
    @TableField("join_time")
    private LocalDateTime joinTime;
}
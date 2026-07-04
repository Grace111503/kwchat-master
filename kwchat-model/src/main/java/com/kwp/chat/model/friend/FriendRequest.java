package com.kwp.chat.model.friend;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kwp.chat.model.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 好友申请实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_friend_request")
public class FriendRequest extends BaseEntity {

    /**
     * 申请者ID
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 接收者ID
     */
    @TableField("receiver_id")
    private Long receiverId;

    /**
     * 申请消息
     */
    @TableField("message")
    private String message;

    /**
     * 状态（0：待处理，1：已同意，2：已拒绝）
     */
    @TableField("status")
    private Integer status;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;
}
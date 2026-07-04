package com.kwp.chat.model.friend;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kwp.chat.model.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 好友实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_friend")
public class Friend extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 好友ID
     */
    @TableField("friend_id")
    private Long friendId;

    /**
     * 好友备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 分组
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 是否星标好友（0：否，1：是）
     */
    @TableField("is_star")
    private Integer isStar;

    /**
     * 是否拉黑（0：否，1：是）
     */
    @TableField("is_black")
    private Integer isBlack;
}
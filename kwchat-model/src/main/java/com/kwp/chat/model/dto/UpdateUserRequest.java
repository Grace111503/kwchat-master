package com.kwp.chat.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 更新用户信息请求DTO
 */
@Data
public class UpdateUserRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    @Size(min = 2, max = 20, message = "昵称长度在2到20个字符")
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别（0：未知，1：男，2：女）
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 个性签名
     */
    @Size(max = 100, message = "个性签名最多100个字符")
    private String signature;

    /**
     * 部门
     */
    @Size(max = 50, message = "部门名称最多50个字符")
    private String department;
}
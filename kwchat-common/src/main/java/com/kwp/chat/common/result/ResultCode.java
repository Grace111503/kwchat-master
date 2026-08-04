package com.kwp.chat.common.result;

import lombok.Getter;

/**
 * 状态码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),

    // 认证相关 1xxx
    UNAUTHORIZED(1001, "未登录或Token已过期"),
    FORBIDDEN(1002, "没有权限"),
    TOKEN_INVALID(1003, "Token无效"),
    TOKEN_EXPIRED(1004, "Token已过期"),
    ACCOUNT_LOCKED(1005, "账号已被锁定"),
    ACCOUNT_DISABLED(1006, "账号已被禁用"),
    LOGIN_FAILED(1007, "用户名或密码错误"),
    REGISTER_FAILED(1008, "注册失败"),
    VERIFY_CODE_ERROR(1009, "验证码错误"),
    VERIFY_CODE_EXPIRED(1010, "验证码已过期"),

    // 用户相关 2xxx
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_ALREADY_EXISTS(2002, "用户已存在"),
    PASSWORD_ERROR(2003, "密码错误"),
    OLD_PASSWORD_ERROR(2004, "旧密码错误"),
    NICKNAME_ALREADY_EXISTS(2005, "昵称已存在"),
    PHONE_ALREADY_EXISTS(2006, "手机号已注册"),
    EMAIL_ALREADY_EXISTS(2007, "邮箱已注册"),

    // 好友相关 3xxx
    FRIEND_NOT_FOUND(3001, "好友不存在"),
    FRIEND_ALREADY_EXISTS(3002, "已经是好友"),
    FRIEND_REQUEST_NOT_FOUND(3003, "好友请求不存在"),
    FRIEND_REQUEST_ALREADY_SENT(3004, "已发送过好友请求"),
    CANNOT_ADD_SELF(3005, "不能添加自己为好友"),

    // 群组相关 4xxx
    GROUP_NOT_FOUND(4001, "群组不存在"),
    GROUP_ALREADY_EXISTS(4002, "群组已存在"),
    GROUP_MEMBER_ALREADY_EXISTS(4003, "已经是群成员"),
    GROUP_MEMBER_NOT_FOUND(4004, "群成员不存在"),
    GROUP_FULL(4005, "群成员已满"),
    GROUP_OWNER_CANNOT_LEAVE(4006, "群主不能退出群聊"),
    NOT_GROUP_OWNER(4007, "不是群主"),
    NOT_GROUP_MEMBER(4008, "不是群成员"),

    // 消息相关 5xxx
    MESSAGE_NOT_FOUND(5001, "消息不存在"),
    MESSAGE_ALREADY_RECALLED(5002, "消息已撤回"),
    MESSAGE_RECALL_TIMEOUT(5003, "消息已超过撤回时间"),
    MESSAGE_RECALL_FAILED(5004, "消息撤回失败"),
    MESSAGE_SEND_FAILED(5005, "消息发送失败"),
    MESSAGE_TYPE_NOT_SUPPORTED(5006, "不支持的消息类型"),
    USER_BLOCKED(5007, "你已被对方拉黑，无法发送消息"),
    USER_BLOCKED_BY_OTHER(5008, "你拉黑了对方，无法发送消息"),

    // 文件相关 6xxx
    FILE_NOT_FOUND(6001, "文件不存在"),
    FILE_UPLOAD_FAILED(6002, "文件上传失败"),
    FILE_DOWNLOAD_FAILED(6003, "文件下载失败"),
    FILE_SIZE_EXCEEDED(6004, "文件大小超出限制"),
    FILE_TYPE_NOT_SUPPORTED(6005, "不支持的文件类型"),

    // 会话相关 7xxx
    CONVERSATION_NOT_FOUND(7001, "会话不存在"),
    CONVERSATION_ALREADY_EXISTS(7002, "会话已存在"),

    // AI相关 8xxx
    AI_SERVICE_UNAVAILABLE(8001, "AI服务不可用"),
    AI_REQUEST_FAILED(8002, "AI请求失败"),
    AI_RESPONSE_ERROR(8003, "AI响应错误"),

    // 参数相关 9xxx
    PARAM_ERROR(9001, "参数错误"),
    PARAM_MISSING(9002, "参数缺失"),
    PARAM_TYPE_ERROR(9003, "参数类型错误"),

    // 系统相关 10xxx
    SYSTEM_ERROR(10001, "系统错误"),
    SERVICE_UNAVAILABLE(10002, "服务不可用"),
    REQUEST_TIMEOUT(10003, "请求超时"),
    RATE_LIMIT_EXCEEDED(10004, "请求频率超限");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
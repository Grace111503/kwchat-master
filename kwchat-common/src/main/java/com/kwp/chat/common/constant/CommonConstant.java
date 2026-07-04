package com.kwp.chat.common.constant;

/**
 * 通用常量
 */
public class CommonConstant {

    /**
     * Token请求头
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 用户ID请求属性
     */
    public static final String USER_ID_ATTRIBUTE = "userId";

    /**
     * 用户名请求属性
     */
    public static final String USERNAME_ATTRIBUTE = "username";

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大每页大小
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 是
     */
    public static final Integer YES = 1;

    /**
     * 否
     */
    public static final Integer NO = 0;

    /**
     * 删除标志 - 未删除
     */
    public static final Integer NOT_DELETED = 0;

    /**
     * 删除标志 - 已删除
     */
    public static final Integer DELETED = 1;

    /**
     * 状态 - 启用
     */
    public static final Integer STATUS_ENABLE = 1;

    /**
     * 状态 - 禁用
     */
    public static final Integer STATUS_DISABLE = 0;

    /**
     * 性别 - 未知
     */
    public static final Integer GENDER_UNKNOWN = 0;

    /**
     * 性别 - 男
     */
    public static final Integer GENDER_MALE = 1;

    /**
     * 性别 - 女
     */
    public static final Integer GENDER_FEMALE = 2;

    /**
     * 好友申请状态 - 待处理
     */
    public static final Integer FRIEND_REQUEST_PENDING = 0;

    /**
     * 好友申请状态 - 已同意
     */
    public static final Integer FRIEND_REQUEST_AGREED = 1;

    /**
     * 好友申请状态 - 已拒绝
     */
    public static final Integer FRIEND_REQUEST_REJECTED = 2;

    /**
     * 消息类型 - 文本
     */
    public static final Integer MESSAGE_TYPE_TEXT = 1;

    /**
     * 消息类型 - 图片
     */
    public static final Integer MESSAGE_TYPE_IMAGE = 2;

    /**
     * 消息类型 - 文件
     */
    public static final Integer MESSAGE_TYPE_FILE = 3;

    /**
     * 消息类型 - 视频
     */
    public static final Integer MESSAGE_TYPE_VIDEO = 4;

    /**
     * 消息类型 - 语音
     */
    public static final Integer MESSAGE_TYPE_VOICE = 5;

    /**
     * 消息类型 - 系统消息
     */
    public static final Integer MESSAGE_TYPE_SYSTEM = 6;

    /**
     * 消息类型 - 撤回消息
     */
    public static final Integer MESSAGE_TYPE_RECALLED = 7;

    /**
     * 会话类型 - 单聊
     */
    public static final Integer CONVERSATION_TYPE_PRIVATE = 1;

    /**
     * 会话类型 - 群聊
     */
    public static final Integer CONVERSATION_TYPE_GROUP = 2;

    /**
     * 群成员角色 - 普通成员
     */
    public static final Integer GROUP_ROLE_MEMBER = 0;

    /**
     * 群成员角色 - 管理员
     */
    public static final Integer GROUP_ROLE_ADMIN = 1;

    /**
     * 群成员角色 - 群主
     */
    public static final Integer GROUP_ROLE_OWNER = 2;

    /**
     * Redis Key前缀 - 用户Token
     */
    public static final String REDIS_TOKEN_PREFIX = "user:token:";

    /**
     * Redis Key前缀 - 用户信息
     */
    public static final String REDIS_USER_PREFIX = "user:info:";

    /**
     * Redis Key前缀 - 用户在线状态
     */
    public static final String REDIS_ONLINE_PREFIX = "user:online:";

    /**
     * Redis Key前缀 - 验证码
     */
    public static final String REDIS_VERIFY_CODE_PREFIX = "verify:code:";

    /**
     * Redis Key前缀 - 好友申请
     */
    public static final String REDIS_FRIEND_REQUEST_PREFIX = "friend:request:";

    /**
     * Redis Key前缀 - 群组信息
     */
    public static final String REDIS_GROUP_PREFIX = "group:info:";

    /**
     * Redis Key前缀 - 会话信息
     */
    public static final String REDIS_CONVERSATION_PREFIX = "conversation:info:";

    /**
     * Redis Key前缀 - 未读消息数
     */
    public static final String REDIS_UNREAD_PREFIX = "message:unread:";

    /**
     * WebSocket消息类型 - 心跳
     */
    public static final String WS_TYPE_HEARTBEAT = "heartbeat";

    /**
     * WebSocket消息类型 - 文本消息
     */
    public static final String WS_TYPE_TEXT = "text";

    /**
     * WebSocket消息类型 - 图片消息
     */
    public static final String WS_TYPE_IMAGE = "image";

    /**
     * WebSocket消息类型 - 文件消息
     */
    public static final String WS_TYPE_FILE = "file";

    /**
     * WebSocket消息类型 - 视频消息
     */
    public static final String WS_TYPE_VIDEO = "video";

    /**
     * WebSocket消息类型 - 语音消息
     */
    public static final String WS_TYPE_VOICE = "voice";

    /**
     * WebSocket消息类型 - 已读回执
     */
    public static final String WS_TYPE_READ_RECEIPT = "read_receipt";

    /**
     * WebSocket消息类型 - 系统通知
     */
    public static final String WS_TYPE_SYSTEM = "system";

    /**
     * 文件存储路径 - 头像
     */
    public static final String FILE_PATH_AVATAR = "avatar/";

    /**
     * 文件存储路径 - 图片
     */
    public static final String FILE_PATH_IMAGE = "image/";

    /**
     * 文件存储路径 - 文件
     */
    public static final String FILE_PATH_FILE = "file/";

    /**
     * 文件存储路径 - 视频
     */
    public static final String FILE_PATH_VIDEO = "video/";

    /**
     * 文件存储路径 - 语音
     */
    public static final String FILE_PATH_VOICE = "voice/";

    /**
     * 消息撤回时间限制（2分钟）
     */
    public static final long MESSAGE_RECALL_TIME_LIMIT = 2 * 60 * 1000;

    /**
     * 最大文件大小（100MB）
     */
    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    /**
     * 最大图片大小（10MB）
     */
    public static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    /**
     * 最大视频大小（50MB）
     */
    public static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024;

    /**
     * 最大语音大小（5MB）
     */
    public static final long MAX_VOICE_SIZE = 5 * 1024 * 1024;
}
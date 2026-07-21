package com.kwp.chat.service;

/**
 * 消息收藏服务接口
 */
public interface MessageFavoriteService {

    /**
     * 收藏消息
     */
    void favoriteMessage(Long messageId, Long userId);

    /**
     * 取消收藏
     */
    void unfavoriteMessage(Long messageId, Long userId);

    /**
     * 检查是否已收藏
     */
    boolean isFavorited(Long messageId, Long userId);
}

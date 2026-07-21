package com.kwp.chat.service.impl;

import com.kwp.chat.common.exception.BusinessException;
import com.kwp.chat.common.result.ResultCode;
import com.kwp.chat.dao.MessageFavoriteMapper;
import com.kwp.chat.dao.MessageMapper;
import com.kwp.chat.model.message.Message;
import com.kwp.chat.model.message.MessageFavorite;
import com.kwp.chat.service.MessageFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息收藏服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageFavoriteServiceImpl implements MessageFavoriteService {

    private final MessageFavoriteMapper messageFavoriteMapper;
    private final MessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favoriteMessage(Long messageId, Long userId) {
        // 检查是否已收藏
        MessageFavorite existing = messageFavoriteMapper.selectByMessageIdAndUserId(messageId, userId);
        if (existing != null) {
            return; // 已收藏，直接返回
        }

        MessageFavorite favorite = new MessageFavorite();
        favorite.setMessageId(messageId);
        favorite.setUserId(userId);
        messageFavoriteMapper.insert(favorite);

        log.info("消息收藏成功: messageId={}, userId={}", messageId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfavoriteMessage(Long messageId, Long userId) {
        MessageFavorite favorite = messageFavoriteMapper.selectByMessageIdAndUserId(messageId, userId);
        if (favorite != null) {
            favorite.setDeleted(1);
            messageFavoriteMapper.updateById(favorite);
            log.info("取消收藏成功: messageId={}, userId={}", messageId, userId);
        }
    }

    @Override
    public boolean isFavorited(Long messageId, Long userId) {
        return messageFavoriteMapper.selectByMessageIdAndUserId(messageId, userId) != null;
    }

    @Override
    public List<Message> getFavoritedMessages(Long userId) {
        // 获取用户收藏的所有记录
        List<MessageFavorite> favorites = messageFavoriteMapper.selectByUserId(userId);

        // 获取对应的消息
        return favorites.stream()
                .map(fav -> messageMapper.selectById(fav.getMessageId()))
                .filter(msg -> msg != null)
                .collect(Collectors.toList());
    }
}

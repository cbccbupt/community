package com.nowconder.community.dao;

import com.nowconder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询当前用户会话列表，每个会话返回最新的一条私信
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    int selectConversationCount(int userId);

    // 查询某个会话的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量(分为某用户总的未读消息数量，以及某用户某条会话内的未读消息数量)
    int selectLetterUnreadCount(int userId, String conversationId);

    // 添加私信
    int insertMessage(Message message);

    // 修改消息状态
    int updateStatus(List<Integer> ids, int status);

    // 查询某个主题下最新的通知
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询未读通知的数量
    int selectNoticeUnreadCount(int userId, String topic);

    // 查询某个主题所包含的通知列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);

}

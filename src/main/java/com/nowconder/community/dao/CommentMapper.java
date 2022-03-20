package com.nowconder.community.dao;

import com.nowconder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    // 通过comment_id选择comment
    Comment selectCommentById(int id);
    // 通过用户的id获取用户所有该用户对帖子的评论
    // 评论分为对帖子的评论，以及对评论的回复（comment表）
    // 所评论的帖子不能是拉黑状态
    List<Comment> selectCommentsByUser(int userId, int offset, int limit);
    // 某用户发布的对帖子评论的数量
    int selectCountByUser(int userId);


}

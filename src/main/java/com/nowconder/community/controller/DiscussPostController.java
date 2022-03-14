package com.nowconder.community.controller;

import com.nowconder.community.entity.Comment;
import com.nowconder.community.entity.DiscussPost;
import com.nowconder.community.entity.Page;
import com.nowconder.community.entity.User;
import com.nowconder.community.service.CommentService;
import com.nowconder.community.service.DiscussPostService;
import com.nowconder.community.service.UserService;
import com.nowconder.community.util.CommunityConstant;
import com.nowconder.community.util.CommunityUtil;
import com.nowconder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static com.nowconder.community.util.CommunityConstant.ENTITY_TYPE_COMMENT;
import static com.nowconder.community.util.CommunityConstant.ENTITY_TYPE_POST;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"未登录！");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        // 查询作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        // 设置帖子的评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论:给帖子的评论
        // 回复:给评论的回复
        // 获取帖子的评论——评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 评论为ViewObject列表 (获取回复的内容，用户信息)
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if(commentList!=null){
            for (Comment comment:commentList){
                Map<String, Object> commentVo = new HashMap<>();
                // 获取评论的内容
                commentVo.put("comment",comment);
                // 获取发表评论的用户
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                // 获取评论的回复——回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(),0 ,Integer.MAX_VALUE);
                // 回复的Vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if(replyList != null){
                    for(Comment reply:replyList){
                        Map<String, Object> replyVo = new HashMap<>();
                        // 获取该评论的回复
                        replyVo.put("reply",reply);
                        // 发表回复的作者
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        // 获取回复的目标
                        User target = reply.getTargetId() == 0? null:userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);

                // 查询该评论的回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount",replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }
}
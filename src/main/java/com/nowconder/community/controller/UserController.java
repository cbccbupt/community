package com.nowconder.community.controller;

import com.nowconder.community.annotation.LoginRequired;
import com.nowconder.community.entity.Comment;
import com.nowconder.community.entity.DiscussPost;
import com.nowconder.community.entity.Page;
import com.nowconder.community.entity.User;
import com.nowconder.community.service.*;
import com.nowconder.community.util.CommunityConstant;
import com.nowconder.community.util.CommunityUtil;
import com.nowconder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.Multipart;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/user", method = RequestMethod.GET)
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;


    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){ return "/site/setting"; }


    // 上传图片
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage==null){
            model.addAttribute("error","未选择图片");
            return "/site/setting";
        }

        // 获得文件名
        String filename = headerImage.getOriginalFilename();
        // 获得文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error", "文件格式不正确");
            return "/site/setting";
        }

        // 修改上传图片的文件名
        // 生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        // 确定文件存储路径（服务器本地存储路径）
        File dest = new File(uploadPath + "/" + filename);
        try {
            // 将headerImage转存至dest
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传失败，服务器异常！", e);
        }

        // 更新头像访问路径（web路径）
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";
    }

    // 获取头像（由web路径，找本地路径并读取图片
    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response){
        // 获取服务器端存储路径
        filename = uploadPath + "/" + filename;
        // 文件后缀（存储文件的类型）
        String suffix = filename.substring(filename.lastIndexOf("."));
        // 响应图片
        response.setContentType("/image" + suffix);
        try(
                FileInputStream fis = new FileInputStream(filename);
                OutputStream os = response.getOutputStream();
                ) {
                byte[] buffer = new byte[1024];
                int b = 0;
                while((b = fis.read(buffer)) != -1){
                    os.write(buffer,0,b);
                }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        }
    }

    // 修改密码
    @RequestMapping(path = "/updatePassword",method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model){
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(),oldPassword,newPassword);
        if(map==null||map.isEmpty()){
            return "redirect:/logout";
        }
        else{
            model.addAttribute("oldPasswordMsg",map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在！");
        }

        // 用户
        model.addAttribute("user",user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        // 某用户的关注的数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 某用户的粉丝的数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 当前登录的用户（hostholder）是否关注该用户（目前页面上显示的user）
        boolean hasFollowed = false;
        if(hostHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    // 该用户的帖子
    @RequestMapping(path = "/my-post/{userId}",method = RequestMethod.GET)
    public String getMyPost(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在！");
        }

        model.addAttribute("user",user);

        page.setLimit(10);
        page.setPath("/user/my-post/" + userId);
        page.setRows(discussPostService.findDiscussPostRows(userId));

        List<DiscussPost> discussList = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussVOList = new ArrayList<>();
        if(discussList!=null){
            for(DiscussPost post:discussList){
                Map<String, Object> map = new HashMap<>();
                map.put("discussPost",post);
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,userId)); //该帖子的获赞数量
                discussVOList.add(map);
            }
        }
        model.addAttribute("discussPosts",discussVOList);
        return "/site/my-post";
    }

    // 该用户对帖子发表的评论
    @RequestMapping(path = "/my-reply/{userId}",method = RequestMethod.GET)
    public String getMyReply(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在！");
        }

        model.addAttribute("user",user);

        page.setLimit(10);
        page.setPath("/user/my-reply/" + userId);
        page.setRows(commentService.findUserCount(userId));

        List<Comment> commentList = commentService.findCommentByUser(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        if(commentList!=null){
            for(Comment comment:commentList){
                Map<String, Object> map = new HashMap<>();
                map.put("comment",comment);
                // 获取该评论所评价的原帖
                DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("discussPost",post);
                commentVOList.add(map);
            }
        }
        model.addAttribute("comments",commentVOList);
        return "/site/my-reply";
    }

}

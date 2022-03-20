package com.nowconder.community.controller;

import com.nowconder.community.entity.DiscussPost;
import com.nowconder.community.entity.Page;
import com.nowconder.community.entity.SearchResult;
import com.nowconder.community.service.ElasticSearchService;
import com.nowconder.community.service.LikeService;
import com.nowconder.community.service.UserService;
import com.nowconder.community.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class SearchController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){

        // 搜索帖子
        try {
            SearchResult searchResult = elasticSearchService.searchDiscussPost(keyword,(page.getCurrent()-1)*10, page.getLimit());
            List<Map<String,Object>> discussPosts = new ArrayList<>();
            List<DiscussPost> list = searchResult.getList();
            if(list!=null){
                for(DiscussPost discussPost:list){
                    Map<String,Object> map = new HashMap<>();
                    map.put("post",discussPost);
                    map.put("user",userService.findUserById(discussPost.getUserId()));
                    map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPost.getId()));

                    discussPosts.add(map);
                }
            }
            model.addAttribute("discussPosts",discussPosts);
            model.addAttribute("keyword",keyword);
            page.setPath("/search?keyword=" + keyword);
            page.setRows(searchResult.getTotal()==0?0:(int) searchResult.getTotal());
        }
        catch (IOException e){
            logger.error("系统出错，没有数据：" + e.getMessage());
        }
        return "/site/search";
    }
}

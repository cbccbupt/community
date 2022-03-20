//package com.nowconder.community;
//
//import com.nowconder.community.dao.DiscussPostMapper;
//import com.nowconder.community.dao.elasticSearch.DiscussPostRepository;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringRunner;
//
////@RunWith(SpringRunner.class)
//@SpringBootTest
//@ContextConfiguration(classes = CommunityApplication.class)
//public class ElasticsearchTests {
//
//    @Autowired
//    private DiscussPostMapper discussPostMapper;
//
//    @Autowired
//    private DiscussPostRepository discussPostRepository;
//
////    @Autowired
////    @Qualifier("client")
////    private RestHighLevelClient restHighLevelClient;
//
//    @Test
//    public void testInsert(){
//        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
//        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
//        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
//    }
//
//}

package com.nowconder.community;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowconder.community.dao.DiscussPostMapper;
import com.nowconder.community.dao.elasticSearch.DiscussPostRepository;
//import org.junit.Test;
import com.nowconder.community.entity.DiscussPost;
import com.nowconder.community.CommunityApplication;
import com.nowconder.community.dao.DiscussPostMapper;
import com.nowconder.community.dao.elasticSearch.DiscussPostRepository;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
//import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DiscussPostRepository discussPostRepository;
    //    @Autowired
//    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    @Qualifier("client")
    private RestHighLevelClient restHighLevelClient;

    //判断某id的文档（数据库中的行）是否存在
    @Test
    public void testExist(){
        boolean exists = discussPostRepository.existsById(1101);
        System.out.println(exists);
    }

    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134,0,100));
    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我是新人，使劲灌水");
        discussPostRepository.save(post);
    }

    @Test
    public void testDelete(){
        discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }

    // 无高亮搜索
    @Test
    public void noHighLightQuery() throws IOException{
        SearchRequest searchRequest = new SearchRequest("discusspost"); // discusspost是索引名（table名）
        // 构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                // 在discusspost表中的title和content字段中查询“互联网寒冬”
                // matchQuery是模糊查询，会对key进行分词：searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                // termQuery是精准查询：searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                .query(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                // 构建排序条件
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //一个可选项，用于控制允许搜索的时间：searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(0) // 从哪条数据开始查
                .size(10); // 需要得到的数据总数

        // 将SearchSourceBuilder对象添加到搜索请求中
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

//        System.out.println("result:");

//        System.out.println(JSONObject.toJSON(searchResponse));

        List<DiscussPost> list = new LinkedList<>();
        for(SearchHit searchHit:searchResponse.getHits().getHits()){
            DiscussPost discussPost = JSONObject.parseObject(searchHit.getSourceAsString(), DiscussPost.class);
//            System.out.println(discussPost);
            list.add(discussPost);
        }
        System.out.println(list.size());
        for(DiscussPost post:list){
            System.out.println(post);
        }
    }

    // 无亮搜索
    @Test
    public void highLightQuery() throws IOException{
        SearchRequest searchRequest = new SearchRequest("discusspost"); // discusspost是索引名（table名）
        Map<String,Object> map = new HashMap<>();

        // 高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");

        // 构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                // 在discusspost表中的title和content字段中查询“互联网寒冬”
                // matchQuery是模糊查询，会对key进行分词：searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                // termQuery是精准查询：searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                .query(QueryBuilders.multiMatchQuery("互联网寒冬求职","title","content"))
                // 构建排序条件
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //一个可选项，用于控制允许搜索的时间：searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(0) // 从哪条数据开始查
                .size(10) // 需要得到的数据总数
                .highlighter(highlightBuilder); // 高亮设置

        // 将SearchSourceBuilder对象添加到搜索请求中
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

//        System.out.println("result:");

//        System.out.println(JSONObject.toJSON(searchResponse));

        List<DiscussPost> list = new LinkedList<>();

        long total = searchResponse.getHits().getTotalHits().value;

        for(SearchHit searchHit:searchResponse.getHits().getHits()){
            DiscussPost discussPost = JSONObject.parseObject(searchHit.getSourceAsString(), DiscussPost.class);

            // 处理高亮显示结果
            HighlightField titleField = searchHit.getHighlightFields().get("title");
            if(titleField!=null){
                discussPost.setTitle(titleField.getFragments()[0].toString()); // 索引[0]表示只把第一个匹配的搜索词做高亮显示
            }
            HighlightField contentField = searchHit.getHighlightFields().get("content");
            if(contentField!=null){
                discussPost.setTitle(contentField.getFragments()[0].toString()); // 索引[0]表示只把第一个匹配的搜索词做高亮显示
            }

//            System.out.println(discussPost);
            list.add(discussPost);
        }

        map.put("list",list);
        map.put("total",total);

//        if(map.get("list")!=null){
//            for (DiscussPost post : list = (List<DiscussPost>) map.get("list")){
//                System.out.println(post);
//            }
//            System.out.println(map.get("total"));
//        }
        if(list!=null){
            for (DiscussPost post : list){
                System.out.println(post);
            }
            System.out.println(map.get("total"));
        }
    }

}


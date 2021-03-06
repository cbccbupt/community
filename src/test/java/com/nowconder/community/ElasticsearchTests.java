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

    //?????????id?????????????????????????????????????????????
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
        post.setContent("???????????????????????????");
        discussPostRepository.save(post);
    }

    @Test
    public void testDelete(){
        discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }

    // ???????????????
    @Test
    public void noHighLightQuery() throws IOException{
        SearchRequest searchRequest = new SearchRequest("discusspost"); // discusspost???????????????table??????
        // ??????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                // ???discusspost?????????title???content????????????????????????????????????
                // matchQuery????????????????????????key???????????????searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                // termQuery??????????????????searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                .query(QueryBuilders.multiMatchQuery("???????????????","title","content"))
                // ??????????????????
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //??????????????????????????????????????????????????????searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(0) // ????????????????????????
                .size(10); // ???????????????????????????

        // ???SearchSourceBuilder??????????????????????????????
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

    // ????????????
    @Test
    public void highLightQuery() throws IOException{
        SearchRequest searchRequest = new SearchRequest("discusspost"); // discusspost???????????????table??????
        Map<String,Object> map = new HashMap<>();

        // ????????????
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");

        // ??????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                // ???discusspost?????????title???content????????????????????????????????????
                // matchQuery????????????????????????key???????????????searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                // termQuery??????????????????searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                .query(QueryBuilders.multiMatchQuery("?????????????????????","title","content"))
                // ??????????????????
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //??????????????????????????????????????????????????????searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(0) // ????????????????????????
                .size(10) // ???????????????????????????
                .highlighter(highlightBuilder); // ????????????

        // ???SearchSourceBuilder??????????????????????????????
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

//        System.out.println("result:");

//        System.out.println(JSONObject.toJSON(searchResponse));

        List<DiscussPost> list = new LinkedList<>();

        long total = searchResponse.getHits().getTotalHits().value;

        for(SearchHit searchHit:searchResponse.getHits().getHits()){
            DiscussPost discussPost = JSONObject.parseObject(searchHit.getSourceAsString(), DiscussPost.class);

            // ????????????????????????
            HighlightField titleField = searchHit.getHighlightFields().get("title");
            if(titleField!=null){
                discussPost.setTitle(titleField.getFragments()[0].toString()); // ??????[0]??????????????????????????????????????????????????????
            }
            HighlightField contentField = searchHit.getHighlightFields().get("content");
            if(contentField!=null){
                discussPost.setTitle(contentField.getFragments()[0].toString()); // ??????[0]??????????????????????????????????????????????????????
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


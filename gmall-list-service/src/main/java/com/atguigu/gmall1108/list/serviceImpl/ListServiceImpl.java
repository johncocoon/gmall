/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: ListServiceImpl
 * Author:   John
 * Date:     2018/4/18 17:13
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.list.serviceImpl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall1108.bean.SkuInfo;
import com.atguigu.gmall1108.bean.SkuLsInfo;
import com.atguigu.gmall1108.bean.SkuLsParams;
import com.atguigu.gmall1108.bean.SkuLsResult;
import com.atguigu.gmall1108.config.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/18
 * @since 1.0.0
 */
@Service
public class ListServiceImpl  implements ListService {

    public static final  String ES_INDEX="gmall1108_2";
    public static final  String ES_TYPE="SkuInfo";

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private JestClient jestClient;
    @Reference
    ManageService  manageService;

    //这个是用于  保存数据  到elasticsearch中  创建数据库和表 相当于
    public void saveSkuInfoById(String skuid){
        SkuInfo skuInfo = manageService.getSkuInfo(skuid);
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        try {
            BeanUtils.copyProperties(skuLsInfo, skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        Index index = new Index.Builder(skuLsInfo).id(skuid).index(ES_INDEX).type(ES_TYPE).build();
        DocumentResult execute=null;
        try {
            execute  = jestClient.execute(index);
            jestClient.close();
            System.out.println(execute.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchQuery(){
        SkuLsParams skuLsParam=new SkuLsParams();
        skuLsParam.setCatalog3Id("61");
        skuLsParam.setValueId(new String[]{"25","23"});

        skuLsParam.setKeyword("小米");

        skuLsParam.setPageNo(1);

        skuLsParam.setPageSize(2);



        search(  skuLsParam);
    }


    //这个是用于查询  elasticsearch中的信息
    public SkuLsResult search(SkuLsParams skuLsParams){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //过滤
        if(skuLsParams.getKeyword()!=null){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",skuLsParams.getKeyword());
            boolQueryBuilder.must(matchQueryBuilder);
            //高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            highlightBuilder.field("skuName");
            searchSourceBuilder.highlight(highlightBuilder);
        }
        if(skuLsParams.getCatalog3Id()!=null){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }

        if(skuLsParams.getValueId()!=null){
            List<String> strings = Arrays.asList(skuLsParams.getValueId());
            for (String string : strings) {
                TermQueryBuilder termQueryBuilders = new TermQueryBuilder("skuAttrValueList.valueId",string);
              boolQueryBuilder.filter(termQueryBuilders);
            }
        }



        //query
        searchSourceBuilder.query(boolQueryBuilder);


        //排序
        searchSourceBuilder.sort("price",SortOrder.DESC);


        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());
        //聚合
        TermsBuilder TermsBuilder =new TermsBuilder("groupBy_valueId");
        TermsBuilder.field("skuAttrValueList.valueId");

        searchSourceBuilder.aggregation(TermsBuilder);

        System.out.println("searchSourceBuilder + "+searchSourceBuilder.toString());

        Search search = new Search.Builder(searchSourceBuilder.toString()).addIndex(ES_INDEX).addType(ES_TYPE).build();


        SearchResult searchResult =null;
        try {
             searchResult = jestClient.execute(search);
            //jestClient.close();   这个还不能随便的关闭
            String jsonString = searchResult.getJsonString();
            System.out.println("json:    "+jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SkuLsResult skuLsResult = makeSkuLsResultFromString(searchResult,skuLsParams);

        return skuLsResult;

    }
    //用来解析查询的结果
    public SkuLsResult    makeSkuLsResultFromString(SearchResult searchResult,SkuLsParams skuLsParams){
        SkuLsResult skuLsResult=new SkuLsResult();
        List<SkuLsInfo> skuLsInfoList=new ArrayList<>(skuLsParams.getPageSize());

        //获取sku列表
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
            if(hit.highlight!=null&&hit.highlight.size()>0){
                List<String> list = hit.highlight.get("skuName");
                //把带有高亮标签的字符串替换skuName
                String skuNameHl = list.get(0);
                skuLsInfo.setSkuName(skuNameHl);
            }
            skuLsInfoList.add(skuLsInfo);
        }
        skuLsResult.setSkuLsInfoList(skuLsInfoList);
        skuLsResult.setTotal(searchResult.getTotal());

        //取记录个数并计算出总页数
        long totalPage= (searchResult.getTotal() + skuLsParams.getPageSize() -1) / skuLsParams.getPageSize();
        skuLsResult.setTotalPages(  totalPage);

        //取出涉及的属性值id
        List<String> attrValueIdList=new ArrayList<>();
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupBy_valueId");
        if(groupby_attr!=null){
            List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                attrValueIdList.add( bucket.getKey()) ;
            }
            skuLsResult.setAttrValueIdList(attrValueIdList);
        }
        return skuLsResult;
    }



    //redis进行对点击量的排名

    @Override
    public void increment(String skuId){

        Jedis jedis = redisUtil.getJedis();
        Double hotScore = jedis.zincrby("hotScore", 1, skuId);

        //不要时时进行对elasticsearch更新   所以经过一段过程才进行更新   elasticsearch的更新的性能比mysql还要弱的多
        //所以更新的时候分段  或者使用spring定时器进行更新   @Scheduled

        if(hotScore%10==0){
            upDateElasticSearchBySkuId(skuId,hotScore);
        }

        jedis.close();

    }

    private void upDateElasticSearchBySkuId(String skuId,Double hotScore){
        String updateStr="{\n" +
                "  \"doc\": {\n" +
                "    \"hotScore\":"+hotScore+"\n" +
                "  }\n" +
                "}";

        Update update = new Update.Builder(updateStr).index(ES_INDEX).type(ES_TYPE).build();
        try {
            DocumentResult execute = jestClient.execute(update);
            System.out.println("  ---------------  "+execute.getJsonString());
            jestClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }





}
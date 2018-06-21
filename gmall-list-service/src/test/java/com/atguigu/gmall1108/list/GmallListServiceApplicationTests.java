package com.atguigu.gmall1108.list;

import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall1108.bean.SkuLsParams;
import com.atguigu.gmall1108.list.serviceImpl.ListServiceImpl;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {


    @Autowired
    private ListService listService;
    @Autowired
    private ListServiceImpl listServiceImpl;

    @Autowired
    JestClient jestClient;


    @Test
    public void test(){
        listService.saveSkuInfoById("6");
        listService.saveSkuInfoById("7");
        listService.saveSkuInfoById("8");

    }
    @Test
    public void test1(){
        SkuLsParams skuLsParam=new SkuLsParams();
        skuLsParam.setCatalog3Id("61");
        skuLsParam.setValueId(new String[]{"25","23"});

        skuLsParam.setKeyword("小米");

        skuLsParam.setPageNo(1);

        skuLsParam.setPageSize(2);



        listServiceImpl.search(  skuLsParam);

    }









    @Test
    public void contextLoads() throws IOException {
            String str="{\n" +
                    "  \"query\": {\n" +
                    "    \"match\": {\n" +
                    "      \"name\": \"红海\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "  \n" +
                    "}";
            Search search = new Search.Builder(str).addIndex("movie_chn").addType("movie").build();
            SearchResult searchResult = jestClient.execute(search);

            List<SearchResult.Hit<HashMap, Void>> hits = searchResult.getHits(HashMap.class);
            for (SearchResult.Hit<HashMap, Void> hit : hits) {
                HashMap source = hit.source;
                System.out.println("source   "+source);
            }

    }

}

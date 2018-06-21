/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: ItemController
 * Author:   John
 * Date:     2018/4/14 15:29
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall1108.annonation.LoginRequire;
import com.atguigu.gmall1108.bean.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 〈一句话功能简述〉<br>
 * 〈页面〉
 *
 * @author John
 * @create 2018/4/14
 * @since 1.0.0
 */
@Controller
public class ItemController {


    @Reference
    private ListService listService;
    @Reference
    private ManageService manageService;


    @RequestMapping("/{skuId}.html")
    public String index(@PathVariable("skuId") String skuId, Map<String, Object> maps) {


        //1 根据id查询出所属的spu的  图片
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        List<SkuImage> skuImageList = manageService.getSkuImage(skuId);
        skuInfo.setSkuImageList(skuImageList);
        //查询spu   这个就是已经处理过额度  页面的显示
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckBySku(Long.parseLong(skuInfo.getId()), Long.parseLong(skuInfo.getSpuId()));
        maps.put("spuSaleAttrList", spuSaleAttrList);
        //将spu下的组合都找到   组合到一个map中去   这个是要放到页面隐藏的
        List<SkuSaleAttrValue> skuSaleAttrValueList= manageService.getSkuSaleAttrValueBySku(Long.parseLong(skuInfo.getSpuId()));
        Map valueKeys = new HashMap();
        //1 现将spu下的skuattrvalue给遍历出来   7|9 1
        String str="";
        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);
            String saleAttrValueId = skuSaleAttrValue.getSaleAttrValueId();
            if(str.length()!=0){
                str=str+"|";
            }
            str=str+saleAttrValueId;
            if(i+1==skuSaleAttrValueList.size()||!skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i+1).getSkuId())){
                valueKeys.put(str,skuSaleAttrValue.getSkuId());
                str="";
            }
        }
        String jsonString = JSON.toJSONString(valueKeys);
        maps.put("valueKeys", jsonString);
        maps.put("skuInfo", skuInfo);
        //计数器  redis
       // listService.increment(skuId);
        return "item";
    }


}
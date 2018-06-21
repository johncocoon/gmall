/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: ListController
 * Author:   John
 * Date:     2018/4/20 11:42
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.llistweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall1108.annonation.LoginRequire;
import com.atguigu.gmall1108.bean.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.*;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/20
 * @since 1.0.0
 */
@Controller
public class ListController {


    @Reference
    private ListService listService;

    @Reference
    ManageService manageService;


    @RequestMapping("/list.html")
    public String list(SkuLsParams skuLsParams,Map<String,Object> map){
        //1 查询sku
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        map.put("skuLsInfoList",skuLsInfoList);

        //如何点击销售属性的跳转  让后查询出来
        String urlParam = makeUrlParam(skuLsParams);
        //2 查询销售属性
        List<BaseAttrInfo> attrList = manageService.getAttrList(skuLsResult.getAttrValueIdList());
        List<BaseAttrValue> valueIdSelected = new ArrayList<>();
        if(skuLsParams.getValueId()!=null){
            for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = iterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    baseAttrValue.setUrlParam(urlParam);
                    String[] valueId = skuLsParams.getValueId();
                    for (String s : valueId) {
                        if(baseAttrValue.getId().equals(s)){
                            iterator.remove();
                            //上面的属性
                            BaseAttrValue attrValue = new BaseAttrValue();
                            attrValue.setValueName(baseAttrInfo.getAttrName()+" : "+baseAttrValue.getValueName());
                            //将当前的查询值给带到地址中
                            attrValue.setUrlParam(makeUrlParam((skuLsParams),baseAttrValue.getId()));
                            valueIdSelected.add(attrValue);
                        }
                    }
                }
            }
        }

        for (BaseAttrInfo baseAttrInfo : attrList) {
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setUrlParam(urlParam);

            }
        }

        int totalPages = (int) (skuLsResult.getTotal()%skuLsParams.getPageSize()==0?(skuLsResult.getTotal()/skuLsParams.getPageSize()):(skuLsResult.getTotal()/skuLsParams.getPageSize()+1));
        map.put("totalPages",totalPages);
        map.put("pageNo",skuLsParams.getPageNo());
        map.put("urlParam",urlParam);
        map.put("skuName",skuLsParams.getKeyword());

        //将属性放到上面
        map.put("valueIdSelect",valueIdSelected);

        map.put("attrList",attrList);


        return "list";
    }

    //这个是解决带过来的属性  然后将其拼接成地址
    // 方案一     让其放到list结合中 然后遍历  string拼接
    //方案二      可以直接对字符串进行开刀
    private  String makeUrlParam(SkuLsParams skuLsParams,String ... valueId){
        String urlParam="";
        if(skuLsParams.getKeyword()!=null){
            if(urlParam!=null&&urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="keyword="+skuLsParams.getKeyword();
        }
        if(skuLsParams.getCatalog3Id()!=null){
            if(urlParam!=null&&urlParam.length()>0){
                urlParam+="&";
            }
            urlParam+="catalog3Id="+skuLsParams.getCatalog3Id();
        }

        if(skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
           a: for (int i = 0; i <skuLsParams.getValueId().length ; i++) {
                if(urlParam!=null&&urlParam.length()>0){
                    urlParam+="&";
                }
                //面包屑中就不要valueId的值了  进行匹配
                for (int j = 0; j < valueId.length; j++) {
                  if(valueId[j].equals(skuLsParams.getValueId()[i])){
                      continue a;
                  }
                }
                urlParam+="valueId="+skuLsParams.getValueId()[i];
            }
        }
        return urlParam;
    }


}
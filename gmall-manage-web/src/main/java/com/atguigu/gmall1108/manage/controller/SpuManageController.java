package com.atguigu.gmall1108.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall1108.bean.SpuImage;
import com.atguigu.gmall1108.bean.SpuInfo;
import com.atguigu.gmall1108.bean.SpuSaleAttr;
import com.atguigu.gmall1108.bean.SpuSaleAttrValue;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @param
 * @return
 */
@Controller
public class SpuManageController {

    @Reference
    ManageService manageService;

    @RequestMapping("spuListPage")
    public String getSpuListPage(){
        return "spuListPage";
    }



    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> getSpuInfoList(@RequestParam Map<String,String> map){
        String catalog3Id = map.get("catalog3Id");
        SpuInfo spuInfo =new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> spuInfoList = manageService.getSpuInfoList(spuInfo);
        return spuInfoList;

    }


    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public ResponseEntity<Void> saveSpuInfo(SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return ResponseEntity.ok().build();
    }


    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> getSpuImageList(@RequestParam("spuId") String spuId){
        List<SpuImage> spuImageList = manageService.getSpuImageList(spuId);
        return spuImageList;
    }


    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> getSpuSaleAttrList(HttpServletRequest httpServletRequest){
        String spuId = httpServletRequest.getParameter("spuId");
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            Map map=new HashMap();
            map.put("total",spuSaleAttrValueList.size());
            map.put("rows",spuSaleAttrValueList);
            // String spuSaleAttrValueJson = JSON.toJSONString(map);
            spuSaleAttr.setSpuSaleAttrValueJson(map);
        }


        return spuSaleAttrList;

    }


    //编辑spu页面
    @RequestMapping("getSpuInfoById")
    @ResponseBody
    public SpuInfo getSpuInfoById(String id){

        return manageService.getSpuInfoById(id);
    }

}

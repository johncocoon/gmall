package com.atguigu.gmall1108.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall1108.bean.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @param
 * @return
 */

@Controller
public class SkuManageController {

    @Reference
    ManageService manageService;

    @RequestMapping(value = "saveSkuInfo",method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Void> saveSkuInfo(SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @RequestMapping("/getskuinfoByspuId")
    public List<SkuInfo> getskuinfoListByspuId(@RequestParam("spuId") String spuId){


       return   manageService.getSkuInfoBySpuId(spuId);
    }



}

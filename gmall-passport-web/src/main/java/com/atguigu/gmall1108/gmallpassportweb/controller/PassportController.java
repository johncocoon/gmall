/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: PassportController
 * Author:   John
 * Date:     2018/4/22 22:01
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.gmallpassportweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall1108.bean.UserInfo;
import com.atguigu.gmall1108.gmallpassportweb.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

import java.util.Map;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author John
 * @create 2018/4/22
 * @since 1.0.0
 */
@Controller
public class PassportController {

    @Value("${token.signKey}")
    String signKey;

    @Reference
    UserService userService;


    @RequestMapping("/index")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "index";
    }


    @RequestMapping(value = "/login",method = RequestMethod.POST)
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){
        String remoteAddr = request.getHeader("x-forwarded-for");

        UserInfo usesrinfoQuery = userService.selectUserInfoByUserInfo(userInfo);

        if(usesrinfoQuery!=null){
            Map map=new HashMap();
            map.put("userId",usesrinfoQuery.getId());
            map.put("nickName",usesrinfoQuery.getNickName());

            String token = JwtUtil.encode(signKey, map, remoteAddr);
            return token;
        }else{
            return "fail";
        }


    }
    @RequestMapping(value="/verify",method=RequestMethod.POST)
    @ResponseBody
    public String verify(HttpServletRequest request){
        //String userId = request.getParameter("userId");
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");
        Map map = JwtUtil.decode(token, signKey, currentIp);
        String id = (String) map.get("userId");
        Boolean verify = userService.verify(id);
        return verify.toString();
    }
}
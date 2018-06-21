/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: LoginInterceptor
 * Author:   John
 * Date:     2018/4/22 23:52
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.Interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall1108.annonation.LoginRequire;
import com.atguigu.gmall1108.constant.WebConst;
import com.atguigu.gmall1108.util.CookieUtil;
import com.atguigu.gmall1108.util.HttpclientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
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

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {



    @Override //这个spring容器会自动给你注入比如你需要一个什么类型的  它给你通过这个handler给 你注入到其中使用
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String newToken = request.getParameter("newToken");
        //将t
        if(newToken!=null&&newToken.length()>0){
            CookieUtil.setCookie(request,response,"token",newToken,WebConst.COOKIE_MAXTIME,false);//获取浏览器上的cookie
        }
        //1 进行如果能从cookie把token取出来，进行解析，显示页面上。
        String token = CookieUtil.getCookieValue(request, "token", false);
        String userId=null;
        if(token!=null){
            Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
            String  tokenForDecode= StringUtils.substringBetween(token, ".");
            byte[] bytes = base64UrlCodec.decode(tokenForDecode);
            JSONObject jsonObject = JSON.parseObject(new String(bytes, "utf-8"));
           userId = (String) jsonObject.get("userId");
            String nickName = (String) jsonObject.get("nickName");
            request.setAttribute("nickName",nickName);

        }


        //验证用户是否已经登录   //通过查看方法上面是否有注解  来进行对用户是否登录做验证
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);

        if(methodAnnotation!=null){
            String currentIp = request.getHeader("x-forwarded-for");

            //下来就是验证  通过httpclient来进行访问userManager中verify
            //制作一个工具类
            String result=null;

            Map map = new HashMap();
            map.put("token",token);

            map.put("currentIp",currentIp);
            result= HttpclientUtil.doPost(WebConst.VERIFY_URL, map);

            if("true".equals(result)){
                request.setAttribute("userId",userId); //只有验证过才能取到userId
                    return true;
            }else{
                if(methodAnnotation.autoRedirect()){
                    String url = URLEncoder.encode(request.getRequestURL().toString(), "utf-8");
                    response.sendRedirect(WebConst.LOGIN_URL+"?originUrl="+url);
                    return false;
                }
            }
        }
        return true;
    }
}
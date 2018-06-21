/**
 * Copyright (C), 2015-2018, XXX有限公司
 * FileName: JwtUtil
 * Author:   John
 * Date:     2018/4/22 22:24
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.atguigu.gmall1108.gmallpassportweb.util;

import io.jsonwebtoken.*;

import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author John
 * @create 2018/4/22
 * @since 1.0.0
 */
public class JwtUtil {

    public static String encode(String key, Map<String,Object> params, String salt) {
        if(salt!=null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256,key);

        jwtBuilder = jwtBuilder.setClaims(params);

        String token = jwtBuilder.compact();
        return token;
    }

    public static Map decode(String token,String key,String salt){
        Claims claims=null;
        if (salt!=null){
            key+=salt;
        }
        try {
            claims= Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        } catch ( JwtException e) {
            return null;
        }
        return  claims;
    }


}
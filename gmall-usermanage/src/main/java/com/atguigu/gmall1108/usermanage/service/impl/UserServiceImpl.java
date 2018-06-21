package com.atguigu.gmall1108.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall1108.bean.UserAddress;
import com.atguigu.gmall1108.bean.UserInfo;
import com.atguigu.gmall1108.config.RedisUtil;
import com.atguigu.gmall1108.constant.UserConst;
import com.atguigu.gmall1108.usermanage.mapper.UserAddressMapper;
import com.atguigu.gmall1108.usermanage.mapper.UserInfoMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @param
 * @return
 */
@Service
public class UserServiceImpl implements UserService {



    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    public List<UserInfo> getUserInfoListAll(){
        List<UserInfo> userInfos = userInfoMapper.selectAll();
        UserInfo userinfoQuery =new UserInfo();
        userinfoQuery.setLoginName("chenge");
        List<UserInfo> userInfos1 = userInfoMapper.select(userinfoQuery);

        Example example=new Example(UserInfo.class);
        example.createCriteria().andLike("name" ,"张%").andEqualTo("id","3");
        List<UserInfo> userInfos2 = userInfoMapper.selectByExample(example);

        return userInfos2;
    }

    public void addUser(UserInfo userInfo){
        byte[] bytes = DigestUtils.md5(userInfo.getPasswd());
        try {
            String password = new String(bytes,"utf-8");
            userInfo.setPasswd(password);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        userInfoMapper.insert(userInfo);
    }


    public void updateUser(String id,UserInfo userInfo){
        Example example=new Example(UserInfo.class);
        example.createCriteria().andLike("name" ,"张%").andEqualTo("id","3");
        userInfoMapper.updateByExampleSelective(userInfo,example);

    }

    public List<UserAddress> getUserAddressList(String userId){

        UserAddress userAddress=new UserAddress();
        userAddress.setUserId(userId);

        List<UserAddress> userAddressList = userAddressMapper.select(userAddress);

        return  userAddressList;
    }

    public UserInfo selectUserInfoByUserInfo(UserInfo userInfo){
        //1 有就存到redis中去  然后查询  有就是已经登录
        String password = DigestUtils.md5Hex(userInfo.getPasswd());
        userInfo.setPasswd(password);
        UserInfo userInfoQuery = userInfoMapper.selectOne(userInfo);
        if(userInfoQuery!=null){
            String userInfoKey="user:"+userInfoQuery.getId()+":info";
            Jedis jedis = redisUtil.getJedis();
            String userInfoJson = JSON.toJSONString(userInfo);
            jedis.setex(userInfoKey,60*60,userInfoJson);
            jedis.close();
            return userInfoQuery;

        }
        return null;
    }


    public boolean verify(String userId){
        String userInfoKey="user:"+userId+":info";
        Jedis jedis = redisUtil.getJedis();

        Boolean exists = jedis.exists(userInfoKey);
        if(exists){
            jedis.expire(userInfoKey,UserConst.REDIS_TIME);
        }
        jedis.close();
        return exists;
    }


}

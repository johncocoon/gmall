package com.atguigu.gmall.service;

import com.atguigu.gmall1108.bean.UserAddress;
import com.atguigu.gmall1108.bean.UserInfo;

import java.util.List;

/**
 * @param
 * @return
 */
public interface UserService {

    public List<UserInfo> getUserInfoListAll();

    public void addUser(UserInfo userInfo);

    public void updateUser(String id,UserInfo userInfo);

    public List<UserAddress> getUserAddressList(String userId);

    public UserInfo selectUserInfoByUserInfo(UserInfo userInfo);

    public boolean verify(String userId);

}

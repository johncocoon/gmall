package com.atguigu.gmall1108.usermanage.controller;

import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall1108.bean.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @param
 * @return
 */
@RestController
public class UserController {


    UserService userService;

    @RequestMapping("userList")
    public ResponseEntity<List<UserInfo>> userList(){
        List<UserInfo> userInfoListAll = userService.getUserInfoListAll();
        return  ResponseEntity.ok(userInfoListAll);
    }

    @RequestMapping(value = "/user" ,method = RequestMethod.POST)
    public    ResponseEntity<Void> add(UserInfo userInfo){ ;

        userService.addUser(userInfo);
        System.out.println("userInfo = " + userInfo);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/user" ,method = RequestMethod.PUT)
    public    ResponseEntity<Void> update(UserInfo userInfo){
        userService.updateUser(userInfo.getId(),userInfo);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/user" ,method = RequestMethod.DELETE)
    public    ResponseEntity<Void> delete(UserInfo userInfo){
     //   userService.delete(userInfo);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/user" ,method = RequestMethod.GET)
    public    ResponseEntity<UserInfo> getUserInfo(UserInfo userInfoQuery){
     //UserInfo userInfo = userService.getUserInfo(userInfoQuery);
        return ResponseEntity.ok().body(new UserInfo());
    }


    /**
     *  用来验证是否已经登录的
     */
    @RequestMapping(value = "/verify",method = RequestMethod.POST)
    public String verify(HttpServletRequest request){
        String token = request.getParameter("token");
        return  "";

    }



}

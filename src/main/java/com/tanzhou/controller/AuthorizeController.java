package com.tanzhou.controller;

import com.tanzhou.dto.AccessTokenDTO;
import com.tanzhou.dto.GithubUser;
import com.tanzhou.mapper.UserMapper;
import com.tanzhou.model.User;
import com.tanzhou.provider.GithubProvider;
import com.tanzhou.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class AuthorizeController {

    @Autowired
    private UserService userService;
    @Autowired
    private GithubProvider githubProvider;

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    @Value("${github.client.uri}")
    private String redirectUri;

    @Autowired
    private UserMapper userMapper;
    /**
     * GitHub权限认证登陆
     *  1 当前端点击登陆按钮时从Github获取GithubUser对象,然后将对象封装成User。
     *  2 每次重新登陆时需要先判断User对象是否存在，
     *  不存在即insert，存在即update(由于question对象时通过creator外键关联User的id属性，所有同一个用户，id不能发生改变)
     *  3 将User对象存入到Cookie里面，并以UUID为Token(其中由数据库充当Session的功能)
     * */
    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code,
                           @RequestParam("state") String state,
                           HttpServletResponse response){
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);
        accessTokenDTO.setCode(code);
        accessTokenDTO.setState(state);
        accessTokenDTO.setRedirect_uri(redirectUri);
        String accessToken = githubProvider.getAccessToken(accessTokenDTO);
        GithubUser githubUser = githubProvider.getUser(accessToken);
        if (githubUser!=null && githubUser.getId()!=null){
            //将GitHub上的User信息存储到自定义的User对象中
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setName(githubUser.getName());
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setAvatarUrl(githubUser.getAvatarUrl());
            //将自定义的User对象存储到数据库中，充当Session的作用
            userService.createOrUpdate(user);
            //将token信息传入到Cookie中
            response.addCookie(new Cookie("token",token));
            return "redirect:/";
        }else {
            //登陆失败后打印失败原因
            return "redirect:/";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response){
        //销毁session中的user对象
        request.getSession().removeAttribute("user");
        Cookie cookie = new Cookie("token",null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }

}

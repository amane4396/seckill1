package com.amane.seckill.service.impl;

import com.amane.seckill.mapper.UserMapper;
import com.amane.seckill.pojo.User;
import com.amane.seckill.service.UserService;
import com.amane.seckill.utils.CookieUtil;
import com.amane.seckill.utils.UUIDUtil;
import com.amane.seckill.utils.VerifyLogin;
import com.amane.seckill.vo.LoginVo;
import com.amane.seckill.vo.RespBean;
import com.amane.seckill.vo.RespBeanEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Override
    public RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String phone = loginVo.getPhone();
        String password = loginVo.getPassword();

        if(StringUtils.isEmpty(password) || StringUtils.isEmpty(phone)){
            return RespBean.error(RespBeanEnum.EMPTY_ERROR);
        }
        if(!VerifyLogin.isMobile(phone)){
            return RespBean.error(RespBeanEnum.PHONE_ERROR);
        }
        User user = userMapper.getByPhone(phone);
        System.out.println(user);
        if(user == null){
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        if (password.length()<6){
            return RespBean.error(RespBeanEnum.PASSWORD_ERROR);
        }
        if(!user.getPassword().equals(password)){
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }

        String ticket = UUIDUtil.uuid();
        redisTemplate.opsForValue().set("user:"+ticket,user);
        //request.getSession().setAttribute(ticket,user);

        CookieUtil.setCookie(request,response,"userTicket",ticket);

        return RespBean.success(ticket);
    }

    @Override
    public User getUserByCookie(String userTicket,HttpServletResponse response,HttpServletRequest request) {
        if(StringUtils.isEmpty(userTicket)){
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:"+userTicket);
        if(user != null){
            CookieUtil.setCookie(request,response,"userTicket",userTicket);
        }
        return user;
    }
}

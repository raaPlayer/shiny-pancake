package com.raa.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.raa.reggie.common.R;
import com.raa.reggie.entity.User;
import com.raa.reggie.service.UserService;
import com.raa.reggie.util.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sedMsg(@RequestBody User user, HttpSession session){
        String phone = user.getPhone();
        if(StringUtils.isNotEmpty(phone)){
            String code = ValidateCodeUtils.generateValidateCode4String(4);
            log.info("手机验证码：{}", code);
            //阿里云验证码 略
            session.setAttribute(phone, code);  //可改用固定键名
            return R.success("手机验证码发送成功");
        }
        return R.error("短信发送失败");
    }

    @PostMapping("/login")  //Map<String, String> map
    public R<User> login(@RequestBody Map<String, String> map, HttpSession session){
        String phone = map.get("phone");
        String code = map.get("code");
        Object codeInSession = session.getAttribute(phone);
        if(codeInSession != null && codeInSession.equals(code)){
            session.removeAttribute(phone);
//            session.getAttribute();
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                    .eq(User::getPhone, phone);
            User one = userService.getOne(queryWrapper);
            if(one == null){
                one = new User();
                one.setPhone(phone);
                one.setStatus(1);   //Status默认0
                userService.save(one);
            }
            session.setAttribute("user", one.getId());
            return R.success(one);
        }
        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> logout(HttpSession session){
        session.removeAttribute("user");
        return R.success("退出登录成功");
    }
}

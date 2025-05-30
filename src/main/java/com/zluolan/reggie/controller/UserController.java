package com.zluolan.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zluolan.reggie.common.R;
import com.zluolan.reggie.entity.User;
import com.zluolan.reggie.service.UserService;
import com.zluolan.reggie.utiles.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;


/**
 * 手机端用户管理
 *
 * @author zhu
 * @date 2023-07-07 16:07
 */

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送短信验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        // 获取手机号
        String phone = user.getPhone();
        if (phone != null) {
            // 生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("验证码：{}", code);

            // 调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("瑞吉外卖", "", phone, code);

            // 将生成的验证码保存到Session
            session.setAttribute(phone, code);
            return R.success("手机验证码短信发送成功");
        }
        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     *
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());
        // 获取手机号
        String phone = map.get("phone").toString();
        // 获取验证码
        String code = map.get("code").toString();
        // 从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);
        // 进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）if (codeInSession != null && codeInSession.equals(code))
        if (codeInSession != null && codeInSession.equals(code)) {
            // 登录成功
            // 判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                // 新用户，自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1); // 设置用户状态为启用状态
                userService.save(user);
            }
            session.setAttribute("user", user.getId()); // 将用户id保存到Session中，以便于后续的登录状态判断
            return R.success(user);
        }
        return R.error("登录失败");
    }

    /**
     * 退出登录
     *
     * @param session
     * @return
     */
    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session) {
        // 清除Session中保存的当前登录用户id
        session.removeAttribute("user");
        return R.success("退出登录成功");
    }

}

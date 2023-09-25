package com.seewin.config;

import com.seewin.common.TokenException;
import com.seewin.entity.Result;
import com.seewin.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        System.out.println(request.getRequestURI());
        if (request.getMethod().equals("OPTIONS")){
            return true;
        }

        // 检查请求头中是否包含Token（假设Token放在名为"token"的请求头中）
        String token = request.getHeader("token");
        Result<String> result = new Result();
        result.setData(null);
        result.setCode(500);
//        log.info("token: "+ token);
        if(token != null && !"".equals(token)){
            // 做一些验证逻辑判断，例如检查Token是否有效或过期
            try {
                Claims claims = JwtUtil.parseJWT(token);

            } catch (Exception e) {
                e.printStackTrace();
                throw new TokenException(500,"token非法");
            }
        }

        if (token == null ) {
            throw new TokenException(500,"token不能为空");

        }

        // 已登录，允许继续访问
        return true;
    }
    //访问controller之后 访问视图之前被调用
//    @Override
//    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
//        log.info("--------------处理请求完成后视图渲染之前的处理操作---------------");
//    }
    //访问视图之后被调用
//    @Override
//    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
//        log.info("---------------视图渲染之后的操作-------------------------0");
//    }

}

package com.seewin.controller;

import com.seewin.entity.Result;
import com.seewin.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CommonController {

    @PostMapping("/verifyToken")
    public Result verifyToken(@RequestBody String token){
        try {
            Claims claims = JwtUtil.parseJWT(token);
            return new Result(200, null,"token正常！");
        }
        catch (Exception e){
            return new Result(500, null, e.getMessage());
        }

    }

}

package com.seewin.controller;

import com.seewin.entity.Result;
import com.seewin.entity.User;
import com.seewin.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Slf4j
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody User user) {

        return userService.login(user);
    }

    @PostMapping("/logout")
    public Result logout(@RequestBody User user) {
        return userService.logout(user);
    }

    @GetMapping("/user")
    public Result getUser() {
        return userService.getUser();
    }

    @GetMapping("/user/name")
    public Result getUserName() {
        return userService.getUserName();
    }

    @GetMapping("/user/query")
    public Result getUserByQuery(@RequestParam HashMap<String, String> queryData) {
        return userService.getUserByQuery(queryData);
    }

    @GetMapping("/user/{id}")
    public Result getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @PostMapping("/user")
    public Result addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping("/user")
    public Result updateUserById(@RequestBody User user) {
        return userService.updateUserById(user);
    }

    @DeleteMapping("/user/{id}")
    public Result deleteUserById(@PathVariable Integer id) {
        return userService.deleteUserById(id);
    }

    @PostMapping("/user/updatePwd")
    public Result updatePwd(@RequestBody HashMap pwdMap) {
        return userService.updatePwd(pwdMap);
    }
}
